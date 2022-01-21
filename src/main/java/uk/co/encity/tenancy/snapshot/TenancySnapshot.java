package uk.co.encity.tenancy.snapshot;

import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import uk.co.encity.tenancy.components.TenancyContact;
import uk.co.encity.tenancy.entity.DirectAuthorisationRequest;
import uk.co.encity.tenancy.entity.TenancyProviderStatus;
import uk.co.encity.tenancy.entity.TenancyTenantStatus;
import uk.co.encity.tenancy.entity.VatSettings;
import uk.co.encity.tenancy.events.TenancyCreatedEvent;

import java.time.Instant;
import java.util.*;

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
    private List<String> defaultPortfolio;
    private boolean hmrcVatEnabled;
    private boolean hmrcVatAgentAuthorisationRequestPending;
    private UUID hmrcVatAgentAuthorisationRequestUUID;
    private Instant hmrcVatLastAgentAuthorisedAt;
    private Instant hmrcVatAgentAuthorisationRequestExpiry;

    /**
     * A {@link Map} of VAT Settings containing an entry for each company in the defaultPortfolio
     */
    private Map<String, VatSettings> portfolioDetails;

    /**
     * A collection of {@link DirectAuthorisationRequest}s.  This structure is two-dimensional
     * because a given company can (and usually will) have multiple requests, and there are multiple
     * companies in the portfolio.
     */
    @Deprecated
    private Map<String, Map<UUID, DirectAuthorisationRequest>> directAuthorisationRequests;

    /**
     * To be added - an Access Token structure (map?) for the different types of access needed
     * to different APIs.  This could get complex, but initially it will just need to hold
     * whatever tokens are needed for VAT purposes.
     */

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
        this.defaultPortfolio = new ArrayList<String>();
        this.hmrcVatEnabled = false;
        this.portfolioDetails = null;
        this.directAuthorisationRequests = new HashMap<String, Map<UUID, DirectAuthorisationRequest>>();
        this.hmrcVatAgentAuthorisationRequestPending = false;
        this.hmrcVatAgentAuthorisationRequestUUID = null;
        this.hmrcVatLastAgentAuthorisedAt = Instant.MIN;
        this.hmrcVatAgentAuthorisationRequestExpiry = null;
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
    //public List<String> getDefaultPortfolio() { return this.defaultPortfolio; }
    public boolean isHmrcVatEnabled() { return this.hmrcVatEnabled; }
    public Map<String, VatSettings> getPortfolioDetails() { return this.portfolioDetails; }
    public Map<String, Map<UUID, DirectAuthorisationRequest>> getDirectAuthorisationRequests() {
        return this.directAuthorisationRequests;
    }
    public boolean isHmrcVatAgentAuthorisationRequestPending() { return this.hmrcVatAgentAuthorisationRequestPending; }
    public UUID getHmrcVatAgentAuthorisationRequestUUID() { return this.hmrcVatAgentAuthorisationRequestUUID; }
    public Instant getHmrcVatLastAgentAuthorisedAt() {
        return (this.hmrcVatLastAgentAuthorisedAt == null ? Instant.MIN : this.hmrcVatLastAgentAuthorisedAt);
    }
    public Instant getHmrcVatAgentAuthorisationRequestExpiry() { return this.hmrcVatAgentAuthorisationRequestExpiry; }

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
    public void setDefaultPortfolio(List<String> entityIds) { this.defaultPortfolio = entityIds; }
    public void setHmrcVatEnabled(boolean enabled) { this.hmrcVatEnabled = enabled; }
    public void setPortfolioDetails(Map<String, VatSettings> details) { this.portfolioDetails = details; }
    public void setDirectAuthorisationRequests(Map<String, Map<UUID, DirectAuthorisationRequest>> requests) {
        this.directAuthorisationRequests = requests;
    }
    public void setHmrcVatAgentAuthorisationRequestPending(boolean pending) { this.hmrcVatAgentAuthorisationRequestPending = pending; }
    public void setHmrcVatAgentAuthorisationRequestUUID(UUID uuid) { this.hmrcVatAgentAuthorisationRequestUUID = uuid; }
    public void setHmrcVatLastAgentAuthorisedAt(Instant time) {
        this.hmrcVatLastAgentAuthorisedAt = ( time == null ? Instant.MIN : time );
    }
    public void setHmrcVatAgentAuthorisationRequestExpiry(Instant time) { this.hmrcVatAgentAuthorisationRequestExpiry = time; }
}
