package uk.co.encity.tenancy;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.time.Instant;

public class TenancySnapshot {

    // All the events that may have occurred on a Tenancy (as a result of commands)
    enum TenancyEventType {
        TENANCY_CREATED,
        TENANCY_AUTHORIZER_NOTIFIED,
        TENANCY_UPDATED,
        TENANCY_CLOSED,
        TENANCY_CONFIRMED,
        TENANCY_CLOSURE_CANCELLED,
        TENANCY_SUSPENDED,
        TENANCY_RELEASED,
        TENANCY_STOPPED
    }

    // The statuses of a Tenancy that a Tenant controls
    enum TenancyTenantStatus {
        UNCONFIRMED,
        CONFIRMED,
        PENDING_CLOSURE,
        CLOSED
    }

    // The statuses of a Tenancy that the Provider controls
    enum TenancyProviderStatus {
        ACTIVE,
        SUSPENDED,
        STOPPED
    }

    // The derived overall availability status of a Tenancy
    enum TenancyAvailabilityStatus {
        ENDED,
        DORMANT,
        OPERATIONAL
    }

    @BsonProperty("_id")
    private ObjectId snapshotId;
    private ObjectId tenancyId;
    private String tenancyName;

    private int fromVersion;
    private int toVersion;

    private Instant lastUpdate;

    private String tariff;
    private TenancyContact authorisedContact;
    private TenancyContact billingContact;
    private TenancySnapshot.TenancyTenantStatus tenantStatus;
    private TenancySnapshot.TenancyProviderStatus providerStatus;

    public TenancySnapshot(TenancyCreatedEvent evt, ObjectId tenancyId) {
        this.snapshotId = new ObjectId();
        this.tenancyId = tenancyId;
        this.tenancyName = evt.getDomain();
        this.lastUpdate = evt.getEventDateTime();
        this.fromVersion = this.toVersion = 1;
        this.tariff = evt.getTariff();
        this.authorisedContact = evt.getAuthorisedContact();
        this.billingContact = evt.getBillingContact();
        this.tenantStatus = TenancySnapshot.TenancyTenantStatus.UNCONFIRMED;
        this.providerStatus = TenancySnapshot.TenancyProviderStatus.ACTIVE;
    }

    public @BsonProperty("_id") ObjectId getSnapshotId() { return this.snapshotId; }
    public ObjectId getTenancyId() { return this.tenancyId; }
    public String getName() { return this.tenancyName; }
    public int getFromVersion() { return this.fromVersion; }
    public int getToVersion() { return this.toVersion; }
    public Instant getLastUpdate() { return this.lastUpdate; }
    public String getTariff() { return this.tariff; }
    public TenancyContact getAuthorisedContact() { return this.authorisedContact; }
    public TenancyContact getBillingContact() { return this.billingContact; }
    public TenancySnapshot.TenancyTenantStatus getTenantStatus() { return this.tenantStatus; }
    public TenancySnapshot.TenancyProviderStatus getProviderStatus() { return this.providerStatus; }

}
