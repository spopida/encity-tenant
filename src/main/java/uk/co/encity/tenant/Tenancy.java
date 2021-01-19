package uk.co.encity.tenant;

public class Tenancy {

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

    private String tariff;
    private Contact authorisedContact;
    private Contact billingContact;
    private TenancyTenantStatus tenantStatus;
    private TenancyProviderStatus providerStatus;

    public Tenancy(String tariff, Contact authorisedContact, Contact adminUser, Contact billingContact) {

        this.tariff = tariff;
        this.authorisedContact = authorisedContact;
        this.billingContact = billingContact;
        this.tenantStatus = TenancyTenantStatus.UNCONFIRMED;
        this.providerStatus = TenancyProviderStatus.ACTIVE;

        // TODO: create the first admin user
    }

    public TenancyAvailabilityStatus getAvailabilityStatus() {
        if (this.providerStatus == TenancyProviderStatus.STOPPED || tenantStatus == TenancyTenantStatus.CLOSED) {
            return TenancyAvailabilityStatus.ENDED;
        }

        if (this.providerStatus == TenancyProviderStatus.SUSPENDED || tenantStatus == TenancyTenantStatus.UNCONFIRMED) {
            return TenancyAvailabilityStatus.DORMANT;
        }

        return TenancyAvailabilityStatus.OPERATIONAL;
    }

}
