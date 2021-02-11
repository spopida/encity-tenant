package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.time.Instant;

import uk.co.encity.tenancy.entity.Tenancy;

/**
 * An event that has occurred in relation to a specific {@link Tenancy}
 */
public abstract class TenancyEvent {

    // TODO: Find a way of avoiding leakage of ObjectIds outside of the repository

    @BsonProperty("_id")
    private ObjectId eventId;

    private TenancyEventType eventType;
    private ObjectId tenancyId;
    private Instant eventDateTime;
    private int tenancyVersionNumber;
    private ObjectId commandId;

    /**
     * Default constructor
     */
    public TenancyEvent() {}

    /**
     * Construct the generic aspects of this event.  This constructor should
     * only be called by sub-classes.
     * @param eventType The {@link TenancyEventType} of this event
     * @param tenancyId The id of this event
     * @param version The version of the logical {@link Tenancy} that resulted from this event
     * @param commandId The id of the {@link uk.co.encity.tenancy.commands.TenancyCommand} that resulted in this event
     */
    protected TenancyEvent(
        TenancyEventType eventType,
        ObjectId tenancyId,
        int version,
        ObjectId commandId)
    {
        this.eventId = new ObjectId();
        this.eventType = eventType;
        this.tenancyId = tenancyId;
        this.commandId = commandId;
        this.eventDateTime = Instant.now();
        this.tenancyVersionNumber = version;
    }

    protected TenancyEvent(JsonNode node) {

        // Extract fields from node and set them

        String oidAsText = node.get("_id").asText();
        Object oidAsObj = node.get("_id").get("value");

        this.eventId = new ObjectId(node.get("_id").asText());
        this.eventType = TenancyEventType.valueOf(node.get("eventType").asText());
        this.tenancyId = new ObjectId(node.get("tenancyId").asText());
        this.commandId = new ObjectId(node.get("commandId").asText());
        this.eventDateTime = Instant.parse(node.get("eventDateTime").asText());
        this.tenancyVersionNumber = Integer.parseInt(node.get("tenancyVersionNumber").asText());
    }

    //--------------------- Getters ---------------------------

    @BsonProperty("_id")
    public ObjectId getEventId() { return this.eventId; };

    public TenancyEventType getEventType() { return this.eventType; }

    public ObjectId getTenancyId() { return this.tenancyId; }

    @BsonIgnore
    public String getTenancyIdHex() { return this.tenancyId.toHexString(); }

    public ObjectId getCommandId() { return this.commandId; }

    public Instant getEventDateTime() { return this.eventDateTime; }

    public int getTenancyVersionNumber() { return tenancyVersionNumber; }

    //--------------------- Setters ---------------------------

    @BsonProperty("_id")
    public void setEventId(ObjectId eventId) { this.eventId = eventId; }

    public void setEventType(TenancyEventType eventType) { this.eventType = eventType; }

    public void setTenancyId(ObjectId tenancyId) { this.tenancyId = tenancyId; }

    public void setCommandId(ObjectId commandId) { this.commandId = commandId; }

    public void setEventDateTime(Instant eventDateTime) { this.eventDateTime = eventDateTime; }

    public void setTenancyVersionNumber(int tenancyVersionNumber) { this.tenancyVersionNumber = tenancyVersionNumber; }

    //---------------------------------------------------------

    /**
     * Apply this event to the {@link Tenancy} that it relates to
     * @param target the {@link Tenancy} impacted by the application of this event
     * @return the new incarnation of the {@link Tenancy}
     */
    abstract public Tenancy applyToTenancy(Tenancy target);
    abstract public void addSerializerToModule(SimpleModule module);

    public final Tenancy updateTenancy(Tenancy target) {
        target.setLastUpdateTime( this.eventDateTime);
        target.setVersionNumber( this.tenancyVersionNumber);
        this.applyToTenancy(target);
        return target;
    }

    /**
     * Pass a representation of this event to a given {@link JsonGenerator}
     * @param jGen the {@link JsonGenerator} that will consume the attributes
     * @throws IOException
     * @throws JsonProcessingException
     */
    protected void writeJson(JsonGenerator jGen) throws IOException, JsonProcessingException {
        jGen.writeFieldName("eventId");
        jGen.writeString(this.getEventId().toHexString());
        jGen.writeFieldName("eventType");
        jGen.writeString(this.getEventType().toString());
        jGen.writeFieldName("tenancyId");
        jGen.writeString(this.getTenancyId().toHexString());
        jGen.writeFieldName("eventDateTime");
        jGen.writeObject(this.getEventDateTime());
        return;
    }

}
