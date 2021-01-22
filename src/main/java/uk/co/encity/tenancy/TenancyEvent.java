package uk.co.encity.tenancy;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.time.Instant;

public abstract class TenancyEvent {
    @BsonProperty("_id")
    private ObjectId eventId;

    private TenancySnapshot.TenancyEventType eventType;
    private String domain;
    private String originatingUserId;
    private Instant eventDateTime;
    private TenancySnapshot.TenancyTenantStatus tenantStatus;
    private TenancySnapshot.TenancyProviderStatus providerStatus;
    private int tenancyVersionNumber;
    private ObjectId commandId;

    public TenancyEvent(TenancySnapshot.TenancyEventType eventType, int version, ObjectId commandId, String emailAddr, String userId) {
        this.eventId = new ObjectId();
        this.eventType = eventType;
        this.commandId = commandId;
        this.eventDateTime = Instant.now();
        this.originatingUserId = userId;
        String parts[] = emailAddr.split("@");
        this.domain = parts[1];
        this.tenantStatus = TenancySnapshot.TenancyTenantStatus.UNCONFIRMED;
        this.providerStatus = TenancySnapshot.TenancyProviderStatus.ACTIVE;
        this.tenancyVersionNumber = version;
    }

    public @BsonProperty("_id") ObjectId getEventId() { return this.eventId; };
    public TenancySnapshot.TenancyEventType getEventType() { return this.eventType; }
    public String getDomain() { return this.domain; }
    public ObjectId getCommandId() { return this.commandId; }
    public String getUserId() { return this.originatingUserId; }
    public Instant getEventDateTime() { return this.eventDateTime; }
    public TenancySnapshot.TenancyTenantStatus getTenantStatus() { return this.tenantStatus; }
    public TenancySnapshot.TenancyProviderStatus getProviderStatus() { return this.providerStatus; }
    public int getTenancyVersionNumber() { return tenancyVersionNumber; }
}
