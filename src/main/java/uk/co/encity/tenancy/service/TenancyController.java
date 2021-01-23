package uk.co.encity.tenancy.service;

import static com.mongodb.client.model.Filters.eq;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import uk.co.encity.tenancy.commands.CreateTenancyCommand;
import uk.co.encity.tenancy.commands.CreateTenancyCommandDeserializer;
import uk.co.encity.tenancy.commands.TenancyCommand;
import uk.co.encity.tenancy.events.TenancyCreatedEvent;
import uk.co.encity.tenancy.events.TenancyEventType;

import java.io.IOException;

/**
 * A RESTful web controller that supports actions relating to Tenants.  A Tenant is an organisation with one or more
 * users that uses encity as an agent for the companies that it serves
 */
@RestController
public class TenancyController {

    private static final String TENANT_COLLECTION = "tenancy";
    private final Logger logger = Loggers.getLogger(getClass());

    private ITenancyRepository tenancyRepo;

    public TenancyController(@Autowired ITenancyRepository repo) {
        logger.info("Constructing " + this.getClass().getName());

        this.tenancyRepo = repo;

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

        /**
         * 6. Notify the authoriser
         *
         * Send a command to the notification service to notify the authoriser and request confirmation of
         * the tenancy.  Confirmation will come back to this service (as a PUT?) and if successful, will
         * trigger the creation of an admin user.  Interesting issue: how will we know which admin user to
         * create?  Answer - by going back to the original command...this means we need to know the id of the command.
         * It will have to be retained during the notification workflow (e.g. in the confirm link)
         *
         * --- NO DON'T DO THAT ! --- DO THIS INSTEAD: ---
         * Publish the tenancy created event (and it needs to include the originating command id).  That
         * way, anyone can pick it up and do what they want.  A notification service will subscribe to this
         * event and send an email with a confirm link which will do a PUT back to this service
         */

        // Return a URL in the Location header - this will have to contain a resource id - base64URL of hex id
        response = ResponseEntity.status(HttpStatus.CREATED).body("{ key: \"Hi Adrian\" }");
        return Mono.just(response);
    }

    /**
     * Attempt to get tenancy info.  A tenancy is identified by their internet domain.
     * @param domain the internet domain that identifies the tenancy
     * @return  A Mono that wraps a ResponseEntity containing the response.  Possible
     *          response status codes are INTERNAL_SERVER_ERROR, OK, and NOT_FOUND.
     */
    @GetMapping("/tenancy/{domain}")
    public Mono<ResponseEntity<String>> getTenant(@PathVariable String domain) {
/*
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
*/
        ResponseEntity<String> response = null;
        response = ResponseEntity.status(HttpStatus.OK).body("");
        return Mono.just(response);
    }
}
