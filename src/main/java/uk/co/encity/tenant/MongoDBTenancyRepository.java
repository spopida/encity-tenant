package uk.co.encity.tenant;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Component
public class MongoDBTenancyRepository implements ITenancyRepository {

    private final MongoClient mongoClient;
    private final MongoDatabase db;

    public MongoDBTenancyRepository(@Value("${mongodb.uri}") String mongodbURI, @Value("${tenant.db}") String dbName) {

        ConnectionString connectionString = new ConnectionString(mongodbURI);
        CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                pojoCodecRegistry);
        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .codecRegistry(codecRegistry)
                .build();

        this.mongoClient = MongoClients.create(clientSettings);
        this.db = this.mongoClient.getDatabase(dbName);
    }

    @Override
    public void captureTenantCommand(TenancyCommand.TenancyTenantCommandType commandType, TenancyCommand command) {
        MongoCollection<TenancyCommand> commands = db.getCollection("TenancyCommand", TenancyCommand.class);
        commands.insertOne(command);
    }

    @Override
    public void captureProviderCommand(TenancyCommand.TenancyProviderCommandType commandType, JSONObject command) {

    }

    @Override
    public void captureEvent(Tenancy.TenancyEventType eventType, TenancyCreatedEvent event) {
        MongoCollection<TenancyEvent> events = db.getCollection("TenancyEvent", TenancyEvent.class);
        events.insertOne(event);
    }

    @Override
    public Tenancy getTenancy(String id) {

        return null;
    }

    @Override
    public boolean tenancyExists( String id) {
        return false;

        // Check the entity / event collection (not the commands!)
    }
}
