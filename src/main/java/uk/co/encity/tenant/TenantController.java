package uk.co.encity.tenant;

import static com.mongodb.client.model.Filters.eq;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.io.IOException;

/**
 * A RESTful web controller that supports actions relating to Tenants.  A Tenant is an organisation with one or more
 * users that uses encity as an agent for the companies that it serves
 */
@RestController
public class TenantController {

    private static final String TENANT_COLLECTION = "tenant";
    private final Logger logger = Loggers.getLogger(getClass());

    private ITenancyRepository tenancyRepo;

    public TenantController(@Autowired ITenancyRepository repo) {
        logger.info("Constructing " + this.getClass().getName());

        this.tenancyRepo = repo;

        logger.info("Construction of " + this.getClass().getName() + " is complete");
    }

    /**
     * Attempt to create a new resource in the tenants collection
     * @return  A Mono that wraps a ResponseEntity containing the response.  Possible
     *          response status codes are OK, INTERNAL_SERVER_ERROR, BAD_REQUEST
     */
    @PostMapping("/tenancies")
    public Mono<ResponseEntity<String>> createTenant(@RequestBody String body) {
        final String createTenancyCommandSchema = "command-schemas/create-tenancy-command.json";

        logger.debug("Attempting to create a new tenancy from request body:\n" + body);
        ResponseEntity<String> response = null;
        JSONObject command;

        // Validate the request body against the relevant JSON schema
        try {
            Schema schema = SchemaLoader.load(
                    new JSONObject(
                            new JSONTokener(requireNonNull(getClass().getClassLoader().getResourceAsStream(createTenancyCommandSchema)))
                    )
            );
            command = new JSONObject(new JSONTokener(body));
            schema.validate(command);
            logger.debug("Incoming request body validates against command schema");
        } catch (ValidationException e) {
            logger.warn("Incoming request body does NOT validate against command schema; potential API mis-use!");
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            return Mono.just(response);
        }

        // Check whether the tenancy already exists - if so, then BAD_REQUEST (or let them update it..do that later)

        if (this.tenancyRepo.tenancyExists("anyoldstring")) {
            logger.debug("Rejecting request to create existing tenancy...[more details needed]");
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            return Mono.just(response);
        } else {
            logger.debug("Tenancy does not exist - OK to create");
        }

        // TODO: Change this...get it from a header...but user is not logged in!
        String userId = "anyolduser";

        // De-serialise the command into an object
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

        // Store the command (with any generated fields)
        tenancyRepo.captureTenantCommand(TenancyCommand.TenancyTenantCommandType.CREATE_TENANCY, cmd);

        // Create a tenancy event
        TenancyCreatedEvent evt = new TenancyCreatedEvent(cmd);

        // Record a tenancy event
        tenancyRepo.captureEvent(Tenancy.TenancyEventType.TENANCY_CREATED, evt);

        // Issue a notify authoriser command? (separate gateway?)
        // Issue a create / update user (command) - NOT an actual user or event!!

        response = ResponseEntity.status(HttpStatus.OK).body("{ key: \"Hi Adrian\" }");
        return Mono.just(response);
    }

    /**
     * Attempt to get tenant info.  A tenant is identified by their internet domain.
     * @param domain the internet domain that identifies the tenancy
     * @return  A Mono that wraps a ResponseEntity containing the response.  Possible
     *          response status codes are INTERNAL_SERVER_ERROR, OK, and NOT_FOUND.
     */
    @GetMapping("/tenant/{domain}")
    public Mono<ResponseEntity<String>> getTenant(@PathVariable String domain) {
/*
        logger.debug("Attempting to GET tenant: " + domain);

        ResponseEntity<String> response = null;
        MongoCollection<Document> tenantCollection = null;

        // Get the collection of tenants - if it's not there, that's a serious error
        try {
            tenantCollection = this.db.getCollection(TENANT_COLLECTION);
        } catch (IllegalArgumentException e) {
            logger.error("COULD NOT FIND COLLECTION: " + TENANT_COLLECTION + "; INVESTIGATE");
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        // Now get the tenant; if it's found, then return it in the body (200), else it's a 404
        if (tenantCollection != null) {
            Document found = tenantCollection.find(eq("domain", domain)).first();

            if (found != null) {
                logger.debug("Found tenant: " + domain );
                response = ResponseEntity.status(HttpStatus.OK).body(found.toJson());
            } else {
                logger.debug("Could not find tenant: " + domain);
                response = ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }
*/
        ResponseEntity<String> response = null;
        response = ResponseEntity.status(HttpStatus.OK).body("");
        return Mono.just(response);
    }
}
