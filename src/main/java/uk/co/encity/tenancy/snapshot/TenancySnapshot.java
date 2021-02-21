package uk.co.encity.tenancy.snapshot;

import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import uk.co.encity.tenancy.components.TenancyContact;
import uk.co.encity.tenancy.entity.TenancyProviderStatus;
import uk.co.encity.tenancy.entity.TenancyTenantStatus;
import uk.co.encity.tenancy.events.TenancyCreatedEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * A 'raw' snapshot of a Tenancy at a point in time.  There are no derived fields
 * or business logic in a snapshot.  To obtain a logical entity, all subsequent events
 * up to the desired point in time should be merged with a snapshot
 */
public class TenancySnapshot {

    // TODO: Use Lombok for all the getters and setters (and no args constructor)
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
    private TenancyContact originalAdminUser;
    private TenancyTenantStatus tenantStatus;
    private TenancyProviderStatus providerStatus;
    private UUID confirmUUID;
    private Instant confirmExpiryTime;

    public TenancySnapshot(TenancyCreatedEvent evt) {
        this.snapshotId = new ObjectId();
        this.tenancyId = evt.getTenancyId();
        this.tenancyName = evt.getDomain();
        this.lastUpdate = evt.getEventDateTime();
        this.fromVersion = this.toVersion = 1;
        this.tariff = evt.getTariff();
        this.authorisedContact = evt.getAuthorisedContact();
        this.billingContact = evt.getBillingContact();
        this.originalAdminUser = evt.getAdminUser();
        this.tenantStatus = TenancyTenantStatus.UNCONFIRMED;
        this.providerStatus = TenancyProviderStatus.ACTIVE;
        this.confirmUUID = evt.getConfirmUUID();
        this.confirmExpiryTime = evt.getExpiryTime();
    }

    /**
     * Default constructor used during decoding from database
     */
    public TenancySnapshot() {
        ;
    }

    // Getters
    public @BsonProperty("_id") ObjectId getSnapshotId() { return this.snapshotId; }
    public ObjectId getTenancyId() { return this.tenancyId; }
    public String getName() { return this.tenancyName; }
    public int getFromVersion() { return this.fromVersion; }
    public int getToVersion() { return this.toVersion; }
    public Instant getLastUpdate() { return this.lastUpdate; }
    public String getTariff() { return this.tariff; }
    public TenancyContact getAuthorisedContact() { return this.authorisedContact; }
    public TenancyContact getBillingContact() { return this.billingContact; }
    public TenancyContact getOriginalAdminUser() { return this.originalAdminUser; }
    public TenancyTenantStatus getTenantStatus() { return this.tenantStatus; }
    public TenancyProviderStatus getProviderStatus() { return this.providerStatus; }
    public UUID getConfirmUUID() { return this.confirmUUID; }
    public Instant getConfirmExpiryTime() { return this.confirmExpiryTime; }

    @BsonIgnore public String getDomain() {
        String parts[] = this.authorisedContact.getEmailAddress().split("@");
        return parts[1];
    }

    // Setters
    @BsonProperty("_id") public void setSnapshotId(ObjectId id) { this.snapshotId = id; }
    public void setTenancyId(ObjectId tId) { this.tenancyId = tId; }
    public void setName(String name) { this.tenancyName = name; }
    public void setFromVersion(int fromVersion) { this.fromVersion = fromVersion; }
    public void setToVersion(int toVersion) { this.toVersion = toVersion; }
    public void setLastUpdate(Instant lastUpd) { this.lastUpdate = lastUpd; }
    public void setTariff(String tariff) { this.tariff = tariff; }
    public void setAuthorisedContact(TenancyContact c) { this.authorisedContact = c; }
    public void setBillingContact(TenancyContact c) { this.billingContact = c; }
    public void setOriginalAdminUser(TenancyContact c) { this.originalAdminUser = c; }
    public void setTenantStatus(TenancyTenantStatus s) { this.tenantStatus = s; }
    public void setProviderStatus(TenancyProviderStatus s) { this.providerStatus = s;}
    public void setConfirmUUID(UUID uuid) { this.confirmUUID = uuid; }
    public void setConfirmExpiryTime(Instant time) { this.confirmExpiryTime = time; }
}
