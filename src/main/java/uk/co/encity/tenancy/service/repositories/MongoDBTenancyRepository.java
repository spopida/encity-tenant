package uk.co.encity.tenancy.service.repositories;

import static com.mongodb.client.model.Filters.*;

import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import org.bson.UuidRepresentation;
import org.bson.codecs.UuidCodec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.util.Logger;
import reactor.util.Loggers;
import uk.co.encity.tenancy.commands.CreateTenancyCommand;
import uk.co.encity.tenancy.commands.TenancyCommand;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.events.*;
import uk.co.encity.tenancy.service.TenancyRepository;
import uk.co.encity.tenancy.snapshot.TenancySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Component
public class MongoDBTenancyRepository implements TenancyRepository {

    /**
     * The {@link Logger} for this class
     */
    private final Logger logger = Loggers.getLogger(getClass());

    private final MongoClient mongoClient;
    private final MongoDatabase db;
    private final CodecRegistry codecRegistry;

    public MongoDBTenancyRepository(@Value("${mongodb.uri}") String mongodbURI, @Value("${tenancy.db}") String dbName) {

        ConnectionString connectionString = new ConnectionString(mongodbURI);

        // The following ClassModels are defined with discriminators enabled - this allows polymorphic serialisation and
        // de-serialisation because a type marker (or rather sub-type marker) is included in the BSON.  This avoids having
        // to worry about custom codecs
        ClassModel<TenancyEvent> tenancyEventModel = ClassModel.builder(TenancyEvent.class).enableDiscriminator(true).build();
        ClassModel<TenancyCreatedEvent> tenancyCreatedEventModel = ClassModel.builder(TenancyCreatedEvent.class).enableDiscriminator(true).build();
        ClassModel<TenancyConfirmedEvent> tenancyConfirmedEventModel = ClassModel.builder(TenancyConfirmedEvent.class).enableDiscriminator(true).build();
        ClassModel<TenancyRejectedEvent> tenancyRejectedEventModel = ClassModel.builder(TenancyRejectedEvent.class).enableDiscriminator(true).build();
        ClassModel<PortfolioChangedEvent> portfolioChangedEventModel = ClassModel.builder(PortfolioChangedEvent.class).enableDiscriminator(true).build();
        ClassModel<HmrcVatEnablementChangedEvent> hmrcVatEnablementChangedEventModel = ClassModel.builder(HmrcVatEnablementChangedEvent.class).enableDiscriminator(true).build();
        ClassModel<HmrcVatAuthorisationRequestedEvent> hmrcVatAuthorisationRequestedEventModel = ClassModel.builder(HmrcVatAuthorisationRequestedEvent.class).enableDiscriminator(true).build();
        // As an alternative to the above, we could probably use @BsonDiscriminator annotations on the classes concerned.  But
        // I don't see that being any 'better' than the above, and at least we are keeping these concerns inside the
        // repository implementation

        PojoCodecProvider tenancyEventPojoCodecProvider = PojoCodecProvider.builder().register(
                tenancyEventModel,
                tenancyCreatedEventModel,
                tenancyConfirmedEventModel,
                tenancyRejectedEventModel,
                portfolioChangedEventModel,
                hmrcVatEnablementChangedEventModel,
                hmrcVatAuthorisationRequestedEventModel)
            .build();

        CodecRegistry pojoCodecRegistry = fromProviders(
            tenancyEventPojoCodecProvider,
            PojoCodecProvider.builder().automatic(true).build());

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
     * Create an initial tenancy snapshot from a {@link CreateTenancyCommand} object.
     * @param evt the {@link TenancyCreatedEvent} that led to this initial snapshot
     */
    @Override
    public void captureTenancySnapshot(TenancyCreatedEvent evt) {
        TenancySnapshot snapshot = new TenancySnapshot(evt);
        MongoCollection<TenancySnapshot> tenancySnapshots = db.getCollection("tenancy_snapshots", TenancySnapshot.class);
        tenancySnapshots.insertOne(snapshot);
    }


    @Override
    public void captureTenancyCommand(TenancyCommand.TenancyTenantCommandType commandType, TenancyCommand command) {
        MongoCollection<TenancyCommand> commands = db.getCollection("tenancy_commands", TenancyCommand.class);
        commands.insertOne(command);
    }

    @Override
    public void captureProviderCommand(TenancyCommand.TenancyProviderCommandType commandType, JSONObject command) {
    }

    @Override
    public void captureEvent(TenancyEventType eventType, TenancyEvent event) {
        //TODO: eventType param is redundant
        MongoCollection<TenancyEvent> events = db.getCollection("tenancy_events", TenancyEvent.class);
        events.insertOne(event);
    }

    /**
     * Apply every event since the snapshot was created in chronological order so that a logical
     * {@link Tenancy} object is inflated
     * @param snap the base snapshot to inflate
     * @return the {@link Tenancy} implied by the snapshot merged with all subsequent events
     */
    private Tenancy inflate(TenancySnapshot snap) throws IOException {
        Tenancy t = null;
        if (snap != null) {
            // Make an entity from the snapshot
            t = Tenancy.fromSnapshot(snap);

            // Get all events since - in chronological order
            List<TenancyEvent> events = getEventRange(t.getHexTenancyId(), snap.getToVersion());
            for (TenancyEvent e : events) {
                t = e.updateTenancy(t);
            }
        }

        return t;
    }

    @Override
    public Tenancy getTenancy(String id) throws IOException {
        TenancySnapshot latestSnap = this.getLatestSnapshot(id);
        return this.inflate(latestSnap);
    }

    @Override
    public Tenancy getTenancyFromDomain(String domain) throws IOException {
        TenancySnapshot latestSnap = this.getLatestSnapshot("domain", domain);
        return this.inflate(latestSnap);
    }

    @Override
    public List<TenancyEvent> getEventRange(String tenancyId, int fromVersion) {
        List<TenancyEvent> evtList = new ArrayList<>();

        MongoCollection<TenancyEvent> events = db.getCollection("tenancy_events", TenancyEvent.class);

        // Define a query that finds the right versions and sorts them
        ObjectId tId = new ObjectId(tenancyId);
        FindIterable<TenancyEvent> evts = events.find(and(eq("tenancyId", tId), gt("tenancyVersionNumber", fromVersion)));

        for (TenancyEvent e : evts) {
            evtList.add(e);
        }
        return evtList;
    }

    @Override
    public TenancySnapshot getLatestSnapshot(String id) {
        TenancySnapshot snap = null;
        try {
            ObjectId targetId = new ObjectId(id);
            MongoCollection<TenancySnapshot> snapshots = db.getCollection("tenancy_snapshots").withDocumentClass(TenancySnapshot.class);
            snap = snapshots.find(eq("tenancyId", targetId)).sort(new BasicDBObject("lastUpdate", -1)).first();
        } catch (IllegalArgumentException e) {
            ; // Swallow the exception and return null (i.e. not found)
        }

        return snap;
    }

    @Override
    public TenancySnapshot getLatestSnapshot(String fieldName, String value) {
        MongoCollection<TenancySnapshot> snapshots = db.getCollection("tenancy_snapshots").withDocumentClass(TenancySnapshot.class);
        TenancySnapshot snap = snapshots.find(eq(fieldName, value)).sort(new BasicDBObject("lastUpdate", -1)).first();

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
