package uk.co.encity.tenancy.service;

import static com.mongodb.client.model.Filters.eq;
import static java.util.Objects.requireNonNull;

//import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.hateoas.server.LinkBuilder;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import uk.co.encity.tenancy.commands.CreateTenancyCommand;
import uk.co.encity.tenancy.commands.CreateTenancyCommandDeserializer;
import uk.co.encity.tenancy.commands.TenancyCommand;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.TenancyProviderStatus;
import uk.co.encity.tenancy.entity.TenancySerializer;
import uk.co.encity.tenancy.entity.TenancyTenantStatus;
import uk.co.encity.tenancy.events.TenancyCreatedEvent;
import uk.co.encity.tenancy.events.TenancyCreatedEventSerializer;
import uk.co.encity.tenancy.events.TenancyEventType;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Instant;

/**
 * A RESTful web controller that supports actions relating to Tenancies.  A Tenancy is an organisation with one or more
 * users that uses encity as an agent for the companies that it serves
 */
@RestController
public class TenancyController {

    /**
     * The name of the AMQP exchange used for message publication
     */
    private static final String topicExchangeName = "encity-exchange";

    /**
     * The name of the database collection of tenancies
     */
    private static final String TENANCY_COLLECTION = "tenancy";

    /**
     * The {@link Logger} for this class
     */
    private final Logger logger = Loggers.getLogger(getClass());

    /**
     * The repository of tenancies
     */
    private ITenancyRepository tenancyRepo;

    /**
     * The RabbitMQ helper class
     */
    private RabbitTemplate rabbitTemplate;

    /**
     * Construct an instance with access to a repository of tenancies and a RabbitMQ helper.
     * @param repo the instance of {@link ITenancyRepository} that is used to read and write tenancies
     *             to/from persistent storage
     * @param rabbitTmpl the instance of {@link RabbitTemplate} used for accessing an AMQP service
     */
    public TenancyController(@Autowired ITenancyRepository repo, @Autowired RabbitTemplate rabbitTmpl) {
        logger.info("Constructing " + this.getClass().getName());

        this.tenancyRepo = repo;
        this.rabbitTemplate = rabbitTmpl;
        this.rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());

        logger.info("Construction of " + this.getClass().getName() + " is complete");
    }

    /**
     * Attempt to create a new resource in the tenancies collection
     * @return  A Mono that wraps a ResponseEntity containing the response.  Possible
     *          response status codes are OK, INTERNAL_SERVER_ERROR, BAD_REQUEST
     */
    @PostMapping("/tenancies")
    public Mono<ResponseEntity<String>> createTenant(@RequestBody String body) {
        final String createTenancyCommandSchema = "command-schemas/create-tenancy-command.json";

        logger.debug("Attempting to create a new tenancy from request body:\n" + body);

        ResponseEntity<String> response = null;

        // 1. Validate the request body against the relevant JSON schema
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

        // 2. Check whether the tenancy already exists - if so, then BAD_REQUEST (or let them update it..do that later)
        // TODO: fix this!
        if (this.tenancyRepo.tenancyExists("anyoldstring")) {
            logger.debug("Rejecting request to create existing tenancy...[more details needed]");
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            return Mono.just(response);
        } else {
            logger.debug("Tenancy does not exist - OK to create");
        }

        // TODO: Change this...get it from a header...but user is not logged in!
        String userId = "anyolduser";

        // 3. De-serialise the command into an object and store it
        CreateTenancyCommand cmd = null;

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        // TODO: fix the user id - get it from a header!
        module.addDeserializer(CreateTenancyCommand.class, new CreateTenancyCommandDeserializer().setUserId(userId));
        mapper.registerModule(module);

        try {
            cmd = mapper.readValue(body, CreateTenancyCommand.class);
            logger.debug("Create tenancy command de-serialised successfully");
        } catch (IOException e) {
            logger.error("Error de-serialising create tenancy command: " + e.getMessage());
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            return Mono.just(response);
        }
        tenancyRepo.captureTenantCommand(TenancyCommand.TenancyTenantCommandType.CREATE_TENANCY, cmd);

        // 4. Create the relevant events - in this case just a TenancyCreatedEvent
        TenancyCreatedEvent evt = new TenancyCreatedEvent(cmd);
        tenancyRepo.captureEvent(TenancyEventType.TENANCY_CREATED, evt);

        // 5. Create an initial snapshot for the tenancy
        tenancyRepo.captureTenancySnapshot(evt);

        // 6. Publish an event
        logger.debug("Sending message...");
        module.addSerializer(TenancyCreatedEvent.class, new TenancyCreatedEventSerializer());
        mapper.registerModule(module);
        String jsonEvt;
        try {
            jsonEvt = mapper.writeValueAsString(evt);
            rabbitTemplate.convertAndSend(topicExchangeName, "encity.tenancy.created", jsonEvt);
        } catch (IOException e) {
            logger.error("Error publishing create tenancy event: " + e.getMessage());
        }

        // Return a URL in the Location header - this will have to contain a resource id - base64URL of hex id
        // TODO: Sort this out! Probably  return the JSON of the snapshot..or the event?
        response = ResponseEntity.status(HttpStatus.CREATED).body("{ key: \"Hi Adrian\" }");
        return Mono.just(response);

        // Next refactor - return the tenancy created, plus links for actions (GET, PATCH (confirm, reject))
    }


    /**
     * Attempt to get a JSON representation of a tenancy that requires confirmation by an
     * authorised contact.
     * @param tenancyId the identifier (as a hex string) of the tenancy
     * @param action the action to confirm.  This is currently unnecessary as it is implied by the method name
     *               but it will be retained for now, pending further development.  It is actually ignored right now
     * @param confirmUUID the nonce generated when the tenancy was created to ensure that only a recipient of the
     *                    confirmation URL can enact the confirmation.
     * @return A {@link Tenancy} represented as HAL-compliant JSON object
     */
    @GetMapping("/tenancy/{tenancyId}")
    public Mono<ResponseEntity<EntityModel<Tenancy>>> getUnconfirmedTenancy(
        @PathVariable String tenancyId,
        @RequestParam("action") String action,
        @RequestParam("uuid") String confirmUUID)
    {
        logger.debug("Received request to GET tenancy: " + tenancyId + " for confirmation purposes");
        ResponseEntity<EntityModel<Tenancy>> response = ResponseEntity.status(HttpStatus.OK).body(EntityModel.of(new Tenancy()));
        //ResponseEntity<String> response = ResponseEntity.status(HttpStatus.OK).body("{}");

        // Translate the tenancyId from base64url into a hex string
        // TODO: Move this to a utils function?
        String hexTenancyId = Hex.encodeHexString(Base64.decodeBase64(tenancyId));

        // retrieve the (logical) tenancy entity
        Tenancy target = this.tenancyRepo.getTenancy(hexTenancyId);

        // Is confirmation still pending?
        if (! target.getTenantStatus().equals(TenancyTenantStatus.UNCONFIRMED)) {
            logger.debug("Cannot confirm tenancy as it is not UNCONFIRMED: " + target.getHexTenancyId() + ", status=" + target.getTenantStatus());
            response = ResponseEntity.status(HttpStatus.CONFLICT).build();
            return Mono.just(response);
        }

        // Has the tenancy been suspended?
        if (! target.getProviderStatus().equals(TenancyProviderStatus.ACTIVE)) {
            logger.debug("Cannot confirm tenancy as it is not ACTIVE: " + target.getHexTenancyId() + ", status=" + target.getProviderStatus());
            response = ResponseEntity.status(HttpStatus.CONFLICT).build();
            return Mono.just(response);
        }

        // Does the UUID match for this confirmation attempt?
        if (! target.getConfirmUUIDString().equals(confirmUUID)) {
            logger.warn(
                    "Attempt to confirm a tenancy with mis-matched UUIDs.  Incoming: " + confirmUUID + ", target=" + target.getConfirmUUIDString() + ".\n" +
                    "Repeated attempts with different UUIDs might indicate suspicious activity.");
            response = ResponseEntity.status(HttpStatus.NOT_FOUND).build();
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
        EntityModel<Tenancy> tenancyEntityModel = EntityModel.of(target);
        try {
            Method m = TenancyController.class.getMethod("getUnconfirmedTenancy", String.class, String.class, String.class);
            Link l = linkTo(m, tenancyId, action, confirmUUID).slash("?action=" + action + "&confirmUUID=" + confirmUUID).withSelfRel();

            tenancyEntityModel.add(l);
        } catch (NoSuchMethodException e) {
            logger.error("Failure generating HAL relations - please investigate.  TenancyId: " + target.getHexTenancyId());
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            return Mono.just(response);
        }

        // There's some magic going here that merits further understanding.  The line below appears to
        // convert the object to HAL-compliant JSON (must be functionality in EntityModel class)
        response = ResponseEntity.status(HttpStatus.CREATED).body(tenancyEntityModel);
        return Mono.just(response);
    }

    /**
     * Attempt to get tenancy info.  A tenancy is identified by their internet domain.
     * @param domain the internet domain that identifies the tenancy
     * @return  A Mono that wraps a ResponseEntity containing the response.  Possible
     *          response status codes are INTERNAL_SERVER_ERROR, OK, and NOT_FOUND.
     */
/*

    @GetMapping("/tenancy/{domain}")
    public Mono<ResponseEntity<String>> getTenancy(@PathVariable String domain) {
        logger.debug("Attempting to GET tenancy: " + domain);

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
