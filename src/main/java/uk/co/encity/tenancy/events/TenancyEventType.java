package uk.co.encity.tenancy.events;

// All the events that may have occurred on a Tenancy (as a result of commands)
public enum TenancyEventType {
    TENANCY_CREATED,
    TENANCY_UPDATED,
    TENANCY_CLOSED,
    TENANCY_CONFIRMED,
    TENANCY_REJECTED,
    TENANCY_CLOSURE_CANCELLED,
    TENANCY_SUSPENDED,
    TENANCY_RELEASED,
    TENANCY_STOPPED,
    PORTFOLIO_CHANGED,
    HMRC_VAT_ENABLEMENT_CHANGED,
    HMRC_VAT_AGENT_AUTHORISATION_REQUESTED
}

