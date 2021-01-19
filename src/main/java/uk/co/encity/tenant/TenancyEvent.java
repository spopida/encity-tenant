package uk.co.encity.tenant;

import java.time.Instant;

public abstract class TenancyEvent {
    private Tenancy.TenancyEventType eventType;
    private String domain;
    private String originatingUserId;
    private Instant eventDateTime;
    private Tenancy.TenancyTenantStatus tenantStatus;
    private Tenancy.TenancyProviderStatus providerStatus;

    public TenancyEvent(Tenancy.TenancyEventType eventType, String emailAddr, String userId) {
        this.eventType = eventType;
        this.eventDateTime = Instant.now();
        this.originatingUserId = userId;
        String parts[] = emailAddr.split("@");
        this.domain = parts[1];
        this.tenantStatus = Tenancy.TenancyTenantStatus.UNCONFIRMED;
        this.providerStatus = Tenancy.TenancyProviderStatus.ACTIVE;
    }

    public Tenancy.TenancyEventType getEventType() { return this.eventType; }
    public String getDomain() { return this.domain; }
    public String getUserId() { return this.originatingUserId; }
    public Instant getEventDateTime() { return this.eventDateTime; }
    public Tenancy.TenancyTenantStatus getTenantStatus() { return this.tenantStatus; }
    public Tenancy.TenancyProviderStatus getProviderStatus() { return this.providerStatus; }
}
