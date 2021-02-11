package uk.co.encity.tenancy.service;

import static java.util.Objects.requireNonNull;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import uk.co.encity.tenancy.commands.*;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.TenancyView;
import uk.co.encity.tenancy.entity.TenancyTenantStatus;
import uk.co.encity.tenancy.entity.TenancyProviderStatus;
import uk.co.encity.tenancy.events.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Instant;

/**
 * A RESTful web controller that supports actions relating to Tenancies.  A Tenancy is an organisation with one or more
 * users that uses encity as an agent for the companies that it serves
 */
@CrossOrigin
@RestController
public class TenancyController {

    /**
     * The name of the AMQP exchange used for message publication
     */
    private static final String topicExchangeName = "encity-exchange";

    /**
     * The {@link Logger} for this class
     */
    private final Logger logger = Loggers.getLogger(getClass());

    /**
     * The repository of tenancies
     */
    private final ITenancyRepository tenancyRepo;

    /**
     * The RabbitMQ helper class
     */
    private final RabbitTemplate rabbitTemplate;

    /**
     * Expiry Hours
     */
    @Value("${tenancy.expiryHours:36}")
    private int expiryHours;

    /**
     * Construct an instance with access to a repository of tenancies and a RabbitMQ helper.
     * @param repo the instance of {@link ITenancyRepository} that is used to read and write tenancies
     *             to/from persistent storage
     * @param rabbitTmpl the instance of {@link RabbitTemplate} used for accessing an AMQP service
     */
    public TenancyController(@Autowired ITenancyRepository repo, @Autowired RabbitTemplate rabbitTmpl, @Value("${tenancy.expiryHours:36}") int hrs) {
        logger.info("Constructing " + this.getClass().getName());

        this.tenancyRepo = repo;
        this.rabbitTemplate = rabbitTmpl;
        this.rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        this.expiryHours = hrs;

        logger.info("Construction of " + this.getClass().getName() + " is complete");
    }

    /**
     * Attempt to create a new resource in the tenancies collection
     * @return  A {@link uk.co.encity.tenancy.entity.TenancyView} represented as HAL-compliant JSON object, or an
     *          appropriate HTTP error response
     */
    @PostMapping("/tenancies")
    public Mono<ResponseEntity<EntityModel<TenancyView>>> createTenant(
        @RequestBody String body,
        UriComponentsBuilder uriBuilder)
    {
        // TODO: fix hard-coding of name?
        final String createTenancyCommandSchema = "command-schemas/create-tenancy-command.json";

        logger.debug("Attempting to create a new tenancy from request body:\n" + body);
        ResponseEntity<EntityModel<TenancyView>> response = null;

        // Validate the request body against the relevant JSON schema
        try {
            Schema schema = SchemaLoader.load(
                new JSONObject(
                    new JSONTokener(requireNonNull(getClass().getClassLoader().getResourceAsStream(createTenancyCommandSchema)))
                )
            );
            JSONObject command = new JSONObject(new JSONTokener(body));
            schema.validate(command);
            logger.debug("Incoming request body validates against command schema");
        } catch (ValidationException e) {
            logger.warn("Incoming request body does NOT validate against command schema; potential API mis-use!");
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            return Mono.just(response);
        }

        // De-serialise the command into an object and store it
        CreateTenancyCommand cmd = null;

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(CreateTenancyCommand.class, new CreateTenancyCommandDeserializer());
        mapper.registerModule(module);

        try {
            cmd = mapper.readValue(body, CreateTenancyCommand.class);
            logger.debug("Create tenancy command de-serialised successfully");
        } catch (IOException e) {
            logger.error("Error de-serialising create tenancy command: " + e.getMessage());
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            return Mono.just(response);
        }
        tenancyRepo.captureTenancyCommand(TenancyCommand.TenancyTenantCommandType.CREATE_TENANCY, cmd);

        // Check whether the tenancy already exists - if so, then BAD_REQUEST (or let them update it..do that later)
        String parts[] = cmd.getAuthorisedContact().getEmailAddress().split("@");
        String domain = parts[1];

        if (this.tenancyRepo.tenancyExists(domain)) {
            logger.debug("Rejecting request to create existing tenancy for domain " + domain);
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            return Mono.just(response);
        } else {
            logger.debug("Tenancy for domain " + domain + " does not exist - OK to create...");
        }

        // Create the relevant events - in this case just a TenancyCreatedEvent
        TenancyCreatedEvent evt = new TenancyCreatedEvent(cmd, expiryHours);
        tenancyRepo.captureEvent(TenancyEventType.TENANCY_CREATED, evt);

        // Create an initial snapshot for the tenancy
        tenancyRepo.captureTenancySnapshot(evt);

        // Publish an event
        logger.debug("Sending message...");
        module.addSerializer(TenancyCreatedEvent.class, new TenancyCreatedEventSerializer());
        mapper.registerModule(module);
        String jsonEvt;
        try {
            jsonEvt = mapper.writeValueAsString(evt);
            rabbitTemplate.convertAndSend(topicExchangeName, "encity.tenancy.created", jsonEvt);
        } catch (IOException e) {
            logger.error("Error publishing create tenancy event: " + e.getMessage());
            // But carry on attempting to generate a response to the client
        }

        // So far, so good - now add the necessary HAL relations
        String hexId = evt.getTenancyIdHex();
        EntityModel<TenancyView> tenancyView;
        try {
            tenancyView = EntityModel.of(tenancyRepo.getTenancy(hexId).getView());
            try {
                Method m = TenancyController.class.getMethod("getTenancy", String.class);
                Link l = linkTo(m, hexId).withSelfRel();
                //Link l = linkTo(m, domain).withSelfRel();

                tenancyView.add(l);
            } catch (NoSuchMethodException e) {
                logger.error("Failure generating HAL relations - please investigate.  TenancyId: " + hexId);
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                return Mono.just(response);
            }
        } catch (Exception e) {
            logger.error("Unexpected error getting tenancy details for response - please investigate: " + hexId);
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            return Mono.just(response);
        }

        // And a location header
        UriComponents uriComponents = uriBuilder.path("/tenancies/" + hexId).build();
        HttpHeaders headers =  new HttpHeaders();
        headers.setLocation(uriComponents.toUri());

        // All done
        response = ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(tenancyView);
        return Mono.just(response);

        // Next refactor - add links for other actions (GET, PATCH (confirm, reject))
    }

    /**
     * Attempt to patch a tenancy
     */
    @PatchMapping(value = "/tenancy/{id}")
    public Mono<ResponseEntity<EntityModel<TenancyView>>> patchTenancy(
        @PathVariable String id,
        @RequestBody String body,
        UriComponentsBuilder uriBuilder)
    {
        logger.debug("Attempting to patch a tenancy from request body:\n" + body);
        ResponseEntity<EntityModel<TenancyView>> response = null;

        // Figure out the type of update
        //  - validate against a generic patch schema that checks the command is supported
        //  - validate the patch data against a specific schema (when more transitions are implemented)

        // The schema contains an enumeration of possible patch sub-types (confirm, reject, etc)
        final String patchTenancyCommandSchema = "command-schemas/patch-tenancy-command.json";

        try {
            Schema schema = SchemaLoader.load(
                new JSONObject(
                    new JSONTokener(requireNonNull(getClass().getClassLoader().getResourceAsStream(patchTenancyCommandSchema)))
                )
            );
            JSONObject patch = new JSONObject(new JSONTokener(body));
            schema.validate(patch);
            logger.debug("Incoming patch request contains valid request type");

            // As we add more transitions, additional validation will be needed here
        } catch (ValidationException e) {
            logger.warn("Incoming request body does NOT validate against patch schema; potential API mis-use!");
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            return Mono.just(response);
        }

        // De-serialise the command into an object and store it
        PatchTenancyCommand cmd = null;

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(PatchTenancyCommand.class, new PatchTenancyCommandDeserializer(id));
        mapper.registerModule(module);

        try {
            cmd = mapper.readValue(body, PatchTenancyCommand.class);
            logger.debug("Patch tenancy command de-serialised successfully");
        } catch (IOException e) {
            logger.error("Error de-serialising patch tenancy command: " + e.getMessage());
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            return Mono.just(response);
        }
        tenancyRepo.captureTenancyCommand(cmd.getCommandType(), cmd);

        // Check pre-conditions for the command to succeed
        Tenancy t = null;
        try {
            if ((t = this.tenancyRepo.getTenancy(id)) != null) {
                try {
                    cmd.checkPreConditions(t);
                } catch (PreConditionException e) {
                    logger.debug(e.getMessage());
                    response = ResponseEntity.status(HttpStatus.CONFLICT).build();
                    return Mono.just(response);
                }

                // OK - it can be actioned
            } else {
                logger.debug("Cannot patch tenancy " + id + " as it doesn't exist");
                // TODO: Consider providing an alternative error status - BAD_REQUEST doesn't see totally appropriate
                response = ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                return Mono.just(response);
            }
        } catch (IOException e) {
            String msg = "Unexpected failure reading tenancy with id: " + id;
            logger.error(msg);
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            return Mono.just(response);
        }

        // Create and save an event - we've 'done' the command after this
        TenancyEvent evt = cmd.createTenancyEvent(t);
        tenancyRepo.captureEvent(evt.getEventType(), evt);

        // Publish the event
        logger.debug("Sending message...");
        evt.addSerializerToModule(module);
        mapper.registerModule(module);
        String jsonEvt;
        try {
            jsonEvt = mapper.writeValueAsString(evt);
            // TODO: the routingKey below needs to be conditional on the event type !!
            // Alternatively we could have encity.tenancy.patched as a routing key
            rabbitTemplate.convertAndSend(topicExchangeName, "encity.tenancy.confirmed", jsonEvt);
        } catch (IOException e) {
            logger.error("Error publishing tenancy confirmed event: " + e.getMessage());
            // But carry on attempting to generate a response to the client
        }

        String hexId = t.getHexTenancyId();
        EntityModel<TenancyView> tenancyView;
        try {
            tenancyView = EntityModel.of(t.getView());
            try {
                Method m = TenancyController.class.getMethod("getTenancy", String.class);
                Link l = linkTo(m, id).withSelfRel();

                tenancyView.add(l);
            } catch (NoSuchMethodException e) {
                logger.error("Failure generating HAL relations - please investigate.  TenancyId: " + hexId);
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                return Mono.just(response);
            }
        } catch (Exception e) {
            logger.error("Unexpected error getting tenancy details for response - please investigate: " + hexId);
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            return Mono.just(response);
        }

        // Build a response (include the correct location)
        UriComponents uriComponents = uriBuilder.path("/tenancies/" + hexId).build();
        HttpHeaders headers =  new HttpHeaders();
        headers.setLocation(uriComponents.toUri());

        response = ResponseEntity.status(HttpStatus.OK).headers(headers).body(tenancyView);
        return Mono.just(response);
    }

    /**
     * Attempt to get a JSON representation of a tenancy that requires confirmation by an
     * authorised contact.
     * @param tenancyId the identifier (as a hex string) of the tenancy
     * @param action the action to confirm.  This is currently unnecessary as it is implied by the method name
     *               but it will be retained for now, pending further development.  It is actually ignored right now
     * @param confirmUUID the nonce generated when the tenancy was created to ensure that only a recipient of the
     *                    confirmation URL can enact the confirmation.
     * @return A {@link uk.co.encity.tenancy.entity.TenancyView} represented as HAL-compliant JSON object
     */
    @GetMapping(value = "/tenancy/{tenancyId}", params = { "action", "uuid" })
    public Mono<ResponseEntity<EntityModel<TenancyView>>> getUnconfirmedTenancy(
        @PathVariable String tenancyId,
        @RequestParam(value = "action") String action,
        @RequestParam(value = "uuid") String confirmUUID)
    {
        logger.debug("Received request to GET tenancy: " + tenancyId + " for confirmation purposes");
        ResponseEntity<EntityModel<TenancyView>> response = null;

        // Translate the tenancyId from base64url into a hex string
        // TODO: Move this to a utils function?
        //String hexTenancyId = Hex.encodeHexString(Base64.decodeBase64(tenancyId));

        // retrieve the (logical) tenancy entity
        Tenancy target = null;
        try {
            target = this.tenancyRepo.getTenancy(tenancyId);
            if (target == null) {
                response = ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                return Mono.just(response);
            }
        } catch (IOException e) {
            String msg = "Unexpected failure reading tenancy with id: " + tenancyId;
            logger.error(msg);
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            return Mono.just(response);
        }

        // Is confirmation still pending?
        if (! target.getTenantStatus().equals(TenancyTenantStatus.UNCONFIRMED)) {
            logger.debug("Cannot confirm tenancy as it is not UNCONFIRMED: " + tenancyId + ", status=" + target.getTenantStatus());
            response = ResponseEntity.status(HttpStatus.CONFLICT).build();
            return Mono.just(response);
        }

        // Has the tenancy been suspended?
        if (! target.getProviderStatus().equals(TenancyProviderStatus.ACTIVE)) {
            logger.debug("Cannot confirm tenancy as it is not ACTIVE: " + tenancyId + ", status=" + target.getProviderStatus());
            response = ResponseEntity.status(HttpStatus.CONFLICT).build();
            return Mono.just(response);
        }

        // Does the UUID match for this confirmation attempt?
        if (! target.getConfirmUUIDString().equals(confirmUUID)) {
            logger.warn(
                    "Attempt to confirm a tenancy with mis-matched UUIDs.  Incoming: " + confirmUUID + ", target=" + target.getConfirmUUIDString() + ".\n" +
                    "Repeated attempts with different UUIDs might indicate suspicious activity.");
            response = ResponseEntity.status(HttpStatus.CONFLICT).build();
            return Mono.just(response);
        }

        // Has the confirmation window expired?
        int compareResult = Instant.now().compareTo(target.getConfirmExpiryTime());
        if (compareResult > 0) {
            logger.debug("Tenancy confirmation window expired at: " + target.getConfirmExpiryTime().toString());
            response = ResponseEntity.status(HttpStatus.CONFLICT).build();
            return Mono.just(response);
        }

        // So far, so good - now add the necessary HAL relations
        EntityModel<TenancyView> tenancyView;

        try {
            tenancyView = EntityModel.of(target.getView());

            try {
                Method m = TenancyController.class.getMethod("getUnconfirmedTenancy", String.class, String.class, String.class);
                Link l = linkTo(m, tenancyId, action, confirmUUID).slash("?action=" + action + "&confirmUUID=" + confirmUUID).withSelfRel();

                tenancyView.add(l);
            } catch (NoSuchMethodException e) {
                logger.error("Failure generating HAL relations - please investigate.  TenancyId: " + target.getHexTenancyId());
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                return Mono.just(response);
            }
        }
        catch (Exception e) {
            logger.error("Unexpected error returning tenancy - please investigate: " + target.getHexTenancyId());
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            return Mono.just(response);
        }

        // There's some magic going here that merits further understanding.  The line below appears to
        // convert the object to HAL-compliant JSON (must be functionality in EntityModel class)
        response = ResponseEntity.status(HttpStatus.OK).body(tenancyView);
        return Mono.just(response);
    }

    /**
     * Attempt to get tenancy info.  A tenancy is identified by its domain.
     * @param domain the internet domain that identifies the tenancy
     * @return  A Mono that wraps a ResponseEntity containing the response.  Possible
     *          response status codes are INTERNAL_SERVER_ERROR, OK, and NOT_FOUND.
     */
    @GetMapping(value = "/tenancy/{domain}", params = {})
    public Mono<ResponseEntity<String>> getTenancy(@PathVariable String domain) {
        logger.debug("Attempting to GET tenancy: " + domain);
        ResponseEntity<String> response = null;
        response = ResponseEntity.status(HttpStatus.OK).body("");
        return Mono.just(response);
    }
/*
        ResponseEntity<String> response = null;
        MongoCollection<Document> tenantCollection = null;

        // Get the collection of tenants - if it's not there, that's a serious error
        try {
            tenantCollection = this.db.getCollection(TENANT_COLLECTION);
        } catch (IllegalArgumentException e) {
            logger.error("COULD NOT FIND COLLECTION: " + TENANT_COLLECTION + "; INVESTIGATE");
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        // Now get the tenancy; if it's found, then return it in the body (200), else it's a 404
        if (tenantCollection != null) {
            Document found = tenantCollection.find(eq("domain", domain)).first();

            if (found != null) {
                logger.debug("Found tenancy: " + domain );
                response = ResponseEntity.status(HttpStatus.OK).body(found.toJson());
            } else {
                logger.debug("Could not find tenancy: " + domain);
                response = ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }
        ResponseEntity<String> response = null;
        response = ResponseEntity.status(HttpStatus.OK).body("");
        return Mono.just(response);
    }
*/

}
