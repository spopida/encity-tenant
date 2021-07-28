package uk.co.encity.tenancy.entity;

import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;
import reactor.util.Logger;
import reactor.util.Loggers;
import uk.co.encity.tenancy.components.TenancyContact;
import uk.co.encity.tenancy.snapshot.TenancySnapshot;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Tenancy {

    /**
     * The {@link Logger} for this class
     */
    private final Logger logger = Loggers.getLogger(getClass());

    private ObjectId tenancyId;
    private String tenancyName;

    private int version;

    private Instant lastUpdate;

    private String tariff;
    private TenancyContact authorisedContact;
    private TenancyContact billingContact;
    private TenancyContact originalAdminUser;
    private TenancyTenantStatus tenantStatus;
    private TenancyProviderStatus providerStatus;
    private UUID confirmUUID;
    private Instant confirmExpiryTime;
    private String domain;
    private List<String> defaultPortfolio;
    private boolean hmrcVatEnabled;
    private Map<String, VatSettings> portfolioDetails;
    private boolean hmrcVatAuthorisationRequestPending;
    private UUID hmrcVatAuthorisationRequestUUID;
    private Instant hmrcVatLastAuthorisedAt;

    public Tenancy() {}

    public static Tenancy fromSnapshot(@NonNull TenancySnapshot snap) {
        Tenancy t = new Tenancy();
        t.tenancyId = snap.getTenancyId();
        t.tariff = snap.getTariff();
        t.tenancyName = snap.getName();
        t.lastUpdate = snap.getLastUpdate();
        t.version = snap.getToVersion();
        t.authorisedContact = snap.getAuthorisedContact();
        t.billingContact = snap.getBillingContact();
        t.originalAdminUser = snap.getOriginalAdminUser();
        t.tenantStatus = snap.getTenantStatus();
        t.providerStatus = snap.getProviderStatus();
        t.confirmUUID = snap.getConfirmUUID();
        t.confirmExpiryTime = snap.getConfirmExpiryTime();
        t.domain = snap.getDomain();
        t.defaultPortfolio = snap.getDefaultPortfolio();
        t.hmrcVatEnabled = snap.isHmrcVatEnabled();
        t.hmrcVatAuthorisationRequestPending = snap.isHmrcVatAuthorisationRequestPending();
        t.hmrcVatAuthorisationRequestUUID = snap.getHmrcVatAuthorisationRequestUUID();
        t.hmrcVatLastAuthorisedAt = snap.getHmrcVatLastAuthorisedAt();

        return t;
    }

    public ObjectId getTenancyId() { return this.tenancyId; }
    public String getHexTenancyId() { return this.tenancyId.toHexString(); }
    public String getName() { return this.tenancyName; }
    public int getVersion() { return this.version; }
    public Instant getLastUpdate() { return this.lastUpdate; }
    public String getTariff() { return this.tariff; }
    public TenancyContact getAuthorisedContact() { return this.authorisedContact; }
    public TenancyContact getBillingContact() { return this.billingContact; }
    public TenancyContact getOriginalAdminUser() { return this.originalAdminUser; }
    public TenancyTenantStatus getTenantStatus() { return this.tenantStatus; }
    public TenancyProviderStatus getProviderStatus() { return this.providerStatus; }
    public UUID getConfirmUUID() { return this.confirmUUID; }
    public String getConfirmUUIDString() { return this.confirmUUID.toString(); }
    public Instant getConfirmExpiryTime() { return this.confirmExpiryTime; }
    public String getDomain() { return this.domain; }
    public List<String> getDefaultPortfolio() { return this.defaultPortfolio; }
    public boolean isHmrcVatEnabled() { return this.hmrcVatEnabled; }
    public Map<String, VatSettings> getPortfolioDetails() { return this.portfolioDetails; }
    public boolean isHmrcVatAuthorisationRequestPending() { return this.hmrcVatAuthorisationRequestPending; }
    public UUID getHmrcVatAuthorisationRequestUUID() { return this.hmrcVatAuthorisationRequestUUID; }
    public Instant getHmrcVatLastAuthorisedAt() { return this.hmrcVatLastAuthorisedAt; }

    /**
     * Get the derived (super) status - useful for simplifying some logic
     * @return the {@link TenancyAvailabilityStatus}
     */
    public TenancyAvailabilityStatus getAvailabilityStatus() {
        if (
            (this.tenantStatus == TenancyTenantStatus.CLOSED || this.tenantStatus == TenancyTenantStatus.REJECTED) ||
            (this.providerStatus == TenancyProviderStatus.STOPPED))
        {
            return TenancyAvailabilityStatus.ENDED;
        }

        if (this.tenantStatus == TenancyTenantStatus.UNCONFIRMED || this.providerStatus == TenancyProviderStatus.SUSPENDED)
        {
            return TenancyAvailabilityStatus.DORMANT;
        }

        return TenancyAvailabilityStatus.OPERATIONAL;
    }

    public void setAuthorisedContact(TenancyContact authContact) {
        this.authorisedContact = authContact;
    }

    public void setLastUpdateTime(Instant i) {
        this.lastUpdate = i;
    }

    public void setVersionNumber(int v) { this.version = v; }

    public void setTenantStatus(TenancyTenantStatus status) {
        this.tenantStatus = status;
    }

    public void setDefaultPortfolio(List<String> newPortfolio) { this.defaultPortfolio = newPortfolio; }

    public void setPortfolioDetails(Map<String, VatSettings> portfolioDetails) { this.portfolioDetails = portfolioDetails; }

    public void setHmrcVatEnabled(boolean value) { this.hmrcVatEnabled = value; }

    public void setHmrcVatAuthorisationRequestPending(boolean value) { this.hmrcVatAuthorisationRequestPending = value; }

    public void setHmrcVatAuthorisationRequestUUID(UUID uuid) { this.hmrcVatAuthorisationRequestUUID = uuid; }

    public void setHmrcVatLastAuthorisedAt(Instant time) { this.hmrcVatLastAuthorisedAt = time; }

    public TenancyView getView() {

        // Make sure to use getters, not instance vars (there could be logic in them)

        TenancyView view = new TenancyView();

        view.id = this.getHexTenancyId();
        view.name = this.getName();
        view.version = this.getVersion();
        view.lastUpdate = this.getLastUpdate().toString(); // TODO: check format, zone, etc
        view.tariff = this.getTariff();
        view.authorisedContact = this.getAuthorisedContact();
        view.billingContact = this.getBillingContact();
        view.originalAdminUser = this.getOriginalAdminUser();
        view.tenantStatus = this.getTenantStatus().toString();
        view.defaultPortfolio = this.getDefaultPortfolio();
        view.isHmrcVatEnabled = this.isHmrcVatEnabled();
        view.domain = this.getDomain();
        view.portfolioDetails = this.getPortfolioDetails();
        view.isHmrcVatEnabled = this.isHmrcVatEnabled();
        view.isHmrcVatAuthorisationRequestPending = this.isHmrcVatAuthorisationRequestPending();
        view.lastHmrcAuthorisation = this.getHmrcVatLastAuthorisedAt().toString();

        return view;
    }
}
