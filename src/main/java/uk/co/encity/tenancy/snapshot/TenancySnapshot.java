package uk.co.encity.tenancy.snapshot;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import uk.co.encity.tenancy.components.TenancyContact;
import uk.co.encity.tenancy.entity.TenancyProviderStatus;
import uk.co.encity.tenancy.entity.TenancyTenantStatus;
import uk.co.encity.tenancy.events.TenancyCreatedEvent;

import java.time.Instant;

public class TenancySnapshot {

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
    private TenancyTenantStatus tenantStatus;
    private TenancyProviderStatus providerStatus;

    public TenancySnapshot(TenancyCreatedEvent evt) {
        this.snapshotId = new ObjectId();
        this.tenancyId = evt.getTenancyId();
        this.tenancyName = evt.getDomain();
        this.lastUpdate = evt.getEventDateTime();
        this.fromVersion = this.toVersion = 1;
        this.tariff = evt.getTariff();
        this.authorisedContact = evt.getAuthorisedContact();
        this.billingContact = evt.getBillingContact();
        this.tenantStatus = TenancyTenantStatus.UNCONFIRMED;
        this.providerStatus = TenancyProviderStatus.ACTIVE;
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
    public TenancyTenantStatus getTenantStatus() { return this.tenantStatus; }
    public TenancyProviderStatus getProviderStatus() { return this.providerStatus; }

}
