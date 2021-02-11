package uk.co.encity.tenancy.entity;

// The statuses of a Tenancy that a Tenant controls
public enum TenancyTenantStatus {
    UNCONFIRMED,
    REJECTED,
    CONFIRMED,
    PENDING_CLOSURE,
    CLOSED
}
