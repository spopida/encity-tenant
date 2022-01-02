package uk.co.encity.tenancy.service;

import static java.util.Objects.requireNonNull;
//import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;
import org.json.JSONTokener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.hateoas.EntityModel;
//import org.springframework.hateoas.Link;
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
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * A RESTful web controller that supports actions relating to Tenancies.  A Tenancy is an organisation with one or more
 * users that uses encity as an agent for the companies that it serves
 */
@CrossOrigin
@RestController
public class TenancyController {

    /**
     * The name of the schema for a patch command
     */
    private static final String patchTenancyCommandSchema = "command-schemas/patch-tenancy-command.json";

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
    private final TenancyRepository tenancyRepo;

    /**
     * The RabbitMQ helper class
     */
    private final RabbitTemplate rabbitTemplate;

    /**
     * Expiry Hours
     */
    // TODO: remove the @Value annotation below?
    @Value("${tenancy.expiryHours:36}")
    private int expiryHours;

    /**
     * The service that contains the business logic
     */
    private final TenancyService service;

    /**
     * Construct an instance with access to a repository of tenancies and a RabbitMQ helper.
     * @param repo the instance of {@link TenancyRepository} that is used to read and write tenancies
     *             to/from persistent storage
     * @param rabbitTmpl the instance of {@link RabbitTemplate} used for accessing an AMQP service
     * @param service the service that contains the business logic
     * @param hrs the number of hours that will elapse before an unconfirmed tenancy expires
     */
    public TenancyController(
            @Autowired TenancyRepository repo,
            @Autowired RabbitTemplate rabbitTmpl,
            @Autowired TenancyService service,
            @Value("${tenancy.expiryHours:36}") int hrs) {
        logger.info("Constructing " + this.getClass().getName());

        this.tenancyRepo = repo;
        this.rabbitTemplate = rabbitTmpl;
        this.rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());

        this.service = service;
        this.expiryHours = hrs;

        logger.info("Construction of " + this.getClass().getName() + " is complete");
    }

    /**
     * Attempt to create a new resource in the tenancies collection
     * @return  A {@link uk.co.encity.tenancy.entity.TenancyView} represented as HAL-compliant JSON object, or an
     *          appropriate HTTP error response
     */
    @PostMapping("/tenancies")
    public Mono<ResponseEntity<TenancyView>> createTenant(
        @RequestBody String body,
        UriComponentsBuilder uriBuilder)
    {
        // TODO: fix hard-coding of name?
        final String createTenancyCommandSchema = "command-schemas/create-tenancy-command.json";

        logger.debug("Attempting to create a new tenancy from request body:\n" + body);
        ResponseEntity<TenancyView> response = null;

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
            ResponseEntity r = new ResponseEntity<>("The domain of the authorised contact already has an account", HttpStatus.BAD_REQUEST);
            return Mono.just(r);
            /* response = ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            return Mono.just(response); */
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

        try {
            TenancyView tenancyView = tenancyRepo.getTenancy(hexId).getView();

            // And a location header
            UriComponents uriComponents = uriBuilder.path("/tenancies/" + hexId).build();
            HttpHeaders headers =  new HttpHeaders();
            headers.setLocation(uriComponents.toUri());

            // All done
            response = ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(tenancyView);
            return Mono.just(response);
        } catch (IOException e) {
            logger.error("Unexpected error getting tenancy details for response - please investigate: " + hexId);
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            return Mono.just(response);
        }
    }

    /**
     * TODO: Comments needed on structure of a multi-patch
     */
    @PatchMapping(value = "/tenancy/{id}", params = { "multi" })
    public Mono<ResponseEntity<TenancyView>> multiPatchTenancy(
            @PathVariable String id,
            @RequestBody String body,
            UriComponentsBuilder uriBuilder)
    {
        logger.debug("Attempting a multi-patch");

        // Iterate through the array of patches and call a single patch.  Gather the responses so that we can
        // decide what the aggregate response should be at the end

        ResponseEntity<TenancyView> response = null;

        //------------------------------------------------
        // 1. Check the container against the patch schema
        //------------------------------------------------

        JSONObject multiPatch = null;

        try {
            multiPatch = this.validatePatchBody(body);
        } catch (ValidationException e) {
            logger.warn("Incoming request body does NOT validate against patch schema; potential API mis-use!");
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            return Mono.just(response);
        }

        Tenancy t = null;
        Map<String, HttpStatus> results = new HashMap<String, HttpStatus>();

        //--------------------------------------------------------------------------
        // 2. Iterate through the array of patches and call a single patch each time
        //--------------------------------------------------------------------------

        // There are multiple sets of patches
        // - one set for the Tenancy aggregate root
        // - one set (perhaps) for each portfolio member

        JSONArray tenancyPatches = multiPatch.getJSONObject("patches").getJSONArray("tenancyChanges");
        JSONArray portfolioPatches = multiPatch.getJSONObject("patches").getJSONArray("portfolioChanges");

        JSONObject innerPatchJson = null;

        HttpStatus status = HttpStatus.OK;

        try {
            for (Object patchObj : tenancyPatches) {
                innerPatchJson = validatePatchBody(patchObj.toString());
                String innerPatchType = innerPatchJson.get("action").toString();
                t = handlePatch(id, patchObj.toString(), null);
            }

            for (Object portfolioMemberPatchSet : portfolioPatches) {

                JSONObject memberChanges = new JSONObject(new JSONTokener(portfolioMemberPatchSet.toString()));

                // Parse the company number out of the object
                String companyId = memberChanges.getString("companyId");

                // Get the set of patches for this member
                JSONArray memberPatches = memberChanges.getJSONArray("changes");

                // Handle each one
                for (Object memberPatch :  memberPatches ) {
                    t = handlePatch(id, memberPatch.toString(), companyId);
                }
            }
        } catch (ValidationException e) {
            logger.warn("Incoming request body does NOT validate against patch schema; potential API mis-use!");
            status = HttpStatus.BAD_REQUEST;
        } catch (UnsupportedOperationException | IOException e) {
            logger.error(e.getMessage());
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        } catch (IllegalArgumentException | PreConditionException e) {
            logger.info(e.getMessage());
            status = HttpStatus.BAD_REQUEST;
        }

        // Build a response (include the correct location)
        UriComponents uriComponents = uriBuilder.path("/tenancies/" + id).build();
        HttpHeaders headers =  new HttpHeaders();
        headers.setLocation(uriComponents.toUri());

        if ( t == null ){
            response = ResponseEntity.status(status).build();
        } else {
            response = ResponseEntity.status(status).headers(headers).body(t.getView());
        }
        return Mono.just(response);
    }

    private JSONObject validatePatchBody(String body) throws ValidationException {
        Schema schema = SchemaLoader.load(
                new JSONObject(
                        new JSONTokener(requireNonNull(getClass().getClassLoader().getResourceAsStream(patchTenancyCommandSchema)))
                )
        );
        JSONObject patch = new JSONObject(new JSONTokener(body));
        schema.validate(patch);
        logger.debug("Incoming patch request contains valid request type");
        // As we add more transitions, additional validation will be needed here
        return patch;
    }

    private Tenancy handlePatch(String tenancyId, String patchBody, String companyId) throws
            IllegalArgumentException,
            PreConditionException,
            IOException,
            UnsupportedOperationException
    {
        // De-serialise the command into an object and store it
        PatchTenancyCommand cmd = null;

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(PatchTenancyCommand.class, new PatchTenancyCommandDeserializer(tenancyId, companyId));
        mapper.registerModule(module);

        cmd = mapper.readValue(patchBody, PatchTenancyCommand.class);
        logger.debug("Patch tenancy command de-serialised successfully");

        tenancyRepo.captureTenancyCommand(cmd.getCommandType(), cmd);

        //-------------------------------------------------------
        // 2. Execute the command
        //-------------------------------------------------------
        Tenancy t = this.service.applyCommand(cmd, module, mapper);
        //t = this.service.applyCommand(cmd);

        return t;
    }

    /**
     * Attempt to patch a tenancy
     */
    @PatchMapping(value = "/tenancy/{id}")
    public Mono<ResponseEntity<TenancyView>> patchTenancy(
        @PathVariable String id,
        @RequestBody String body,
        UriComponentsBuilder uriBuilder)
    {
        // TODO: REFACTOR - validatePatchBody, handlePatch
        logger.debug("Attempting to patch a tenancy from request body:\n" + body);
        ResponseEntity<TenancyView> response = null;

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

        JSONObject thePatch = new JSONObject(new JSONTokener(body));
        JSONObject value = thePatch.getJSONObject("value");
        String companyId = value.getString("companyNumber");

        // De-serialise the command into an object and store it
        PatchTenancyCommand cmd = null;

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();

        if (companyId == null) {
            module.addDeserializer(PatchTenancyCommand.class, new PatchTenancyCommandDeserializer(id));
        } else {
            module.addDeserializer(PatchTenancyCommand.class, new PatchTenancyCommandDeserializer(id, companyId));
        }

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

        //-------------------------------------------------------
        // 2. Execute the command
        //-------------------------------------------------------
        Tenancy t = null;
        try {
            t = this.service.applyCommand(cmd, module, mapper);
        } catch (UnsupportedOperationException | IOException e) {
            logger.error(e.getMessage());
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            return Mono.just(response);
        } catch (IllegalArgumentException | PreConditionException e) {
            logger.info(e.getMessage());
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            return Mono.just(response);
        }

        String hexId = t.getHexTenancyId();

        // Build a response (include the correct location)
        UriComponents uriComponents = uriBuilder.path("/tenancies/" + hexId).build();
        HttpHeaders headers =  new HttpHeaders();
        headers.setLocation(uriComponents.toUri());

        response = ResponseEntity.status(HttpStatus.OK).headers(headers).body(t.getView());
        return Mono.just(response);
    }

    @GetMapping(value = "/tenancy/{tenancyId}/authorise-hmrc", params = { "uuid" })
    public Mono<ResponseEntity<TenancyView>> getTenancyForHmrcAuthorisation(
        @PathVariable String tenancyId,
        @RequestParam(value = "uuid") String requestUUID)
    {
        logger.debug("Received request to GET tenancy: " + tenancyId + " for HMRC authorisation purposes");
        ResponseEntity<TenancyView> response = null;

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

        // Is there a pending authorisation request?
        if (! target.isHmrcVatAgentAuthorisationRequestPending()) {
            logger.debug("Cannot authorise HMRC access for the tenancy as there is no pending request: " + tenancyId);
            response = ResponseEntity.status(HttpStatus.CONFLICT).build();
            return Mono.just(response);
        }

        // Does the UUID match?
        if (! target.getHmrcVatAuthorisationRequestUUIDString().equals(requestUUID)) {
            logger.warn(
                    "Attempt to authorise HMRC access for a tenancy with mis-matched UUIDs.  Incoming: " + requestUUID + ", target=" + target.getHmrcVatAgentAuthorisationRequestUUID() + ".\n" +
                            "Repeated attempts with different UUIDs might indicate suspicious activity.");
            response = ResponseEntity.status(HttpStatus.CONFLICT).build();
            return Mono.just(response);
        }

        // Has the request expired?
        int compareResult = Instant.now().compareTo(target.getHmrcVatAgentAuthorisationRequestExpiry());
        if (compareResult > 0) {
            logger.debug("HMRC authorisation window expired at: " + target.getHmrcVatAgentAuthorisationRequestExpiry().toString());
            response = ResponseEntity.status(HttpStatus.CONFLICT).build();
            return Mono.just(response);
        }

        // Has the tenancy been suspended?
        if (! target.getProviderStatus().equals(TenancyProviderStatus.ACTIVE)) {
            logger.debug("Cannot authorise HMRC access for the tenancy as it is not ACTIVE: " + tenancyId + ", status=" + target.getProviderStatus());
            response = ResponseEntity.status(HttpStatus.CONFLICT).build();
            return Mono.just(response);
        }

        response = ResponseEntity.status(HttpStatus.OK).body(target.getView());
        return Mono.just(response);
    }

    /**
     * Attempt to get a JSON representation of a tenancy that requires confirmation by an
     * authorised contact.
     * @param tenancyId the identifier (as a hex string) of the tenancy
     * @param confirmUUID the nonce generated when the tenancy was created to ensure that only a recipient of the
     *                    confirmation URL can enact the confirmation.
     * @return A {@link uk.co.encity.tenancy.entity.TenancyView} represented as HAL-compliant JSON object
     */
    @GetMapping(value = "/tenancy/{tenancyId}/confirm", params = { "uuid" })
    public Mono<ResponseEntity<TenancyView>> getUnconfirmedTenancy(
        @PathVariable String tenancyId,
        @RequestParam(value = "uuid") String confirmUUID)
    {
        logger.debug("Received request to GET tenancy: " + tenancyId + " for confirmation purposes");
        ResponseEntity<TenancyView> response = null;

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

        response = ResponseEntity.status(HttpStatus.OK).body(target.getView());
        return Mono.just(response);
    }

    /**
     * Attempt to get tenancy info
     * @param tenancyId the unique id that identifies the tenancy
     * @return  A Mono that wraps a ResponseEntity containing the response.  Possible
     *          response status codes are INTERNAL_SERVER_ERROR, OK, and NOT_FOUND.
     */
    @GetMapping(value = "/tenancy/{tenancyId}", params = { "availability"})
    public Mono<ResponseEntity<TenancyView>> getTenancy(
            @PathVariable String tenancyId,
            @RequestParam(value = "availability") String availability) {
        logger.debug("Received request to GET tenancy: " + tenancyId);

        ResponseEntity<TenancyView> response = null;

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

        if (! availability.equals("operational")) {
            logger.debug(String.format("Invalid request type: availability=%s", availability));
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            return Mono.just(response);
        }

        // Is the tenancy confirmed?
        if (! target.getTenantStatus().equals(TenancyTenantStatus.CONFIRMED)) {
            logger.debug("Tenancy has invalid tenant status: " + tenancyId + ", status=" + target.getTenantStatus());
            response = ResponseEntity.status(HttpStatus.CONFLICT).build();
            return Mono.just(response);
        }

        // Has the tenancy been suspended?
        if (! target.getProviderStatus().equals(TenancyProviderStatus.ACTIVE)) {
            logger.debug("Cannot retrieve tenancy as it is not ACTIVE: " + tenancyId + ", status=" + target.getProviderStatus());
            response = ResponseEntity.status(HttpStatus.CONFLICT).build();
            return Mono.just(response);
        }

        response = ResponseEntity.status(HttpStatus.OK).body(target.getView());
        return Mono.just(response);
    }
}
