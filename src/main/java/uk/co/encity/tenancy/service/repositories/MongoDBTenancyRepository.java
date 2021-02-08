package uk.co.encity.tenancy.service.repositories;

import static com.mongodb.client.model.Sorts.*;
import static com.mongodb.client.model.Filters.*;

import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.codecs.UuidCodec;
import org.bson.codecs.UuidCodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.co.encity.tenancy.commands.CreateTenancyCommand;
import uk.co.encity.tenancy.commands.TenancyCommand;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.events.TenancyCreatedEvent;
import uk.co.encity.tenancy.events.TenancyEvent;
import uk.co.encity.tenancy.events.TenancyEventFactory;
import uk.co.encity.tenancy.events.TenancyEventType;
import uk.co.encity.tenancy.service.ITenancyRepository;
import uk.co.encity.tenancy.snapshot.TenancySnapshot;

import java.util.ArrayList;
import java.util.List;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Component
public class MongoDBTenancyRepository implements ITenancyRepository {

    private final MongoClient mongoClient;
    private final MongoDatabase db;
    private final CodecRegistry codecRegistry;

    public MongoDBTenancyRepository(@Value("${mongodb.uri}") String mongodbURI, @Value("${tenancy.db}") String dbName) {

        ConnectionString connectionString = new ConnectionString(mongodbURI);
        CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        this.codecRegistry = fromRegistries(
                CodecRegistries.fromCodecs(new UuidCodec(UuidRepresentation.STANDARD)),
                MongoClientSettings.getDefaultCodecRegistry(),
                pojoCodecRegistry);
        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .codecRegistry(this.codecRegistry)
                .build();

        this.mongoClient = MongoClients.create(clientSettings);
        this.db = this.mongoClient.getDatabase(dbName);
    }

    /**
     * Create an initial tenancy snapshot from a {@link CreateTenancyCommand} object.  Because
     * this represents the creation event, we also create an identity
     * @param evt the {@link TenancyCreatedEvent} that led to this initial snapshot
     */
    @Override
    public void captureTenancySnapshot(TenancyCreatedEvent evt) {
        /*
        String identity = null;

        // Create an identity that includes immutable identity-related fields (just id and name in this case)
        MongoCollection<Document> identities = db.getCollection("tenancy_identities", Document.class);

        String name = evt.getDomain();
        // TODO: Consider creating a POJO for this
        Document doc = new Document();
        ObjectId tenancyId = new ObjectId();
        doc.append("_id", tenancyId);
        doc.append("name", name);
        identities.insertOne(doc);

         */

        // Create the snapshot
        TenancySnapshot snapshot = new TenancySnapshot(evt);
        MongoCollection<TenancySnapshot> tenancySnapshots = db.getCollection("tenancy_snapshots", TenancySnapshot.class);
        tenancySnapshots.insertOne(snapshot);
    }


    @Override
    public void captureTenantCommand(TenancyCommand.TenancyTenantCommandType commandType, TenancyCommand command) {
        MongoCollection<TenancyCommand> commands = db.getCollection("tenancy_commands", TenancyCommand.class);
        commands.insertOne(command);
    }

    @Override
    public void captureProviderCommand(TenancyCommand.TenancyProviderCommandType commandType, JSONObject command) {
    }

    @Override
    public void captureEvent(TenancyEventType eventType, TenancyCreatedEvent event) {
        MongoCollection<TenancyEvent> events = db.getCollection("tenancy_events", TenancyEvent.class);
        events.insertOne(event);
    }

    @Override
    public Tenancy getTenancy(String id) {

        TenancySnapshot latestSnap = this.getLatestSnapshot(id);

        Tenancy t = null;
        if (latestSnap != null) {
            // Make an entity from the most recent snapshot
            t = Tenancy.fromSnapshot(latestSnap);

            // Get all events since - in chronological order
            List<TenancyEvent> events = getEventRange(latestSnap.getToVersion());
            Tenancy finalT = t;
            events.forEach(e -> e.applyToTenancy(finalT));       // Note that forEach() preserves List iterator order
        }

        return t;
    }

    @Override
    public List<TenancyEvent> getEventRange(int fromVersion) {
        List<TenancyEvent> evtList = new ArrayList<>();

        MongoCollection<Document> events = db.getCollection("tenancy_events");

        // Define a query that finds the right versions and sorts them
        BasicDBObject evtQuery = new BasicDBObject();
        evtQuery.put("tenancyVersionNumber", new BasicDBObject("$gt", fromVersion));

        // Execute the query and for each resulting doc, call the factory to create a concrete
        // TenancyEvent, and insert it in the list to be returned.
        events.find(evtQuery).sort(orderBy(ascending("tenancyVersionNumber"))).forEach(e -> {
            TenancyEvent evt = TenancyEventFactory.getTenancyEvent(
                TenancyEventType.valueOf(e.get("eventType").toString()),
                e.toJson());
            evtList.add(evt);
        });

        return evtList;
    }

    @Override
    public TenancySnapshot getLatestSnapshot(String id) {
        ObjectId targetId = new ObjectId(id);
        MongoCollection<TenancySnapshot> snapshots = db.getCollection("tenancy_snapshots").withDocumentClass(TenancySnapshot.class);
        TenancySnapshot snap = snapshots.find(eq("tenancyId", targetId)).sort(new BasicDBObject("lastUpdate", -1)).first();

        return snap;
    }

    @Override
    public boolean tenancyExists(String domain) {
        // Rely on the fact that there should always be at least one snapshot.  Note that this method doesn't
        // care about the status of a tenancy
        MongoCollection<TenancySnapshot> snapshots = db.getCollection("tenancy_snapshots").withDocumentClass(TenancySnapshot.class);
        TenancySnapshot snap = snapshots.find(eq("name", domain)).limit(1).first();
        return (snap != null);
    }
}
