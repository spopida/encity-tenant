package uk.co.encity.tenant;

import java.time.Instant;

public abstract class TenancyCommand {

    // Defines the commands that a Tenant may perform on a Tenancy
    enum TenancyTenantCommandType {
        CREATE_TENANCY,
        UPDATE_TENANCY,
        CLOSE_TENANCY,
        CONFIRM_TENANCY,
        CANCEL_TENANCY_CLOSURE,
    }

    // Defines the commands that the Provider may perform on a Tenancy
    enum TenancyProviderCommandType {
        SUSPEND_TENANCY,
        RELEASE_TENANCY,
        STOP_TENANCY
    }

    private Instant timeStamp;
    private String originatingUser;

    public TenancyCommand(String userId) {
        this.originatingUser = userId;
        this.timeStamp = Instant.now();
    }

    public String getUserId() {
        return this.originatingUser;
    }

    public Instant getTimeStamp() {
        return this.timeStamp;
    }

}
