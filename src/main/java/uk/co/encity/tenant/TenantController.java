package uk.co.encity.tenant;

import static com.mongodb.client.model.Filters.eq;

import com.fasterxml.jackson.databind.jsonschema.JsonSerializableSchema;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

/**
 * A RESTful web controller that supports actions relating to Tenants.  A Tenant is an organisation with one or more
 * users that uses encity as an agent for the companies that it serves
 */
@RestController
public class TenantController {
    /** TODO:
     * This class is hard-wired to MongoDB.  Not the end of the world, but it would be better
     * to abstract a TenantRepository, and inject a MongoDB implementation of TenantRepository into this
     * controller.
     */

    private static final String TENANT_COLLECTION = "tenant";
    private final Logger logger = Loggers.getLogger(getClass());
    private final MongoClient mongoClient;
    private final MongoDatabase db;

    public TenantController(@Value("${mongodb.uri}") String mongodbURI, @Value("${tenant.db}") String dbName) {
        logger.info("Constructing " + this.getClass().getName());

        // Set up a connection
        this.mongoClient = MongoClients.create(mongodbURI);
        this.db = this.mongoClient.getDatabase(dbName);

        logger.info("Construction of " + this.getClass().getName() + " is complete");
    }

    /**
     * Attempt to create a new resource in the tenants collection
     * @return  A Mono that wraps a ResponseEntity containing the response.  Possible
     *          response status codes are OK, INTERNAL_SERVER_ERROR, BAD_REQUEST
     */
    @PostMapping("/tenants")
    public Mono<ResponseEntity<String>> createTenant(@RequestBody String body) {
        final String newTenantDTOSchemaName = "dto-schemas/new-tenant-dto.json";

        logger.debug("Attempting to create a new tenant from request body:\n" + body);
        ResponseEntity<String> response = null;

        // TODO:
        // - capture the body and check it against a JSON Schema Validator - possible 400 error
        // - convert the body to a POJO (DTO)
        // - check whether the tenancy already exists - if so, then client error (or internal error)
        // - turn the POJO into an entity (aggregate) - add initial state (as part of a repeating group)
        // - convert the entity to Bson / JSON
        // - insert the entity in the datastore

        try {
            JSONObject jsonSchema = new JSONObject(
                new JSONTokener(getClass().getClassLoader().getResourceAsStream(newTenantDTOSchemaName)));
            JSONObject jsonSubject = new JSONObject(new JSONTokener(body));

            Schema schema = SchemaLoader.load(jsonSchema);
            schema.validate(jsonSubject);
            logger.debug("Incoming request body validates against DTO schema");

            response = ResponseEntity.status(HttpStatus.OK).body("{ key: \"Hi Adrian\" }");

        } catch (ValidationException e) {
            logger.warn("Incoming request body does NOT validate against DTO schema; potential API mis-use.");
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

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

        return Mono.just(response);
    }
}
