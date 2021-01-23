package uk.co.encity.tenancy.commands;

import java.time.Instant;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

public abstract class TenancyCommand {

    // Defines the commands that a Tenant may perform on a Tenancy
    public enum TenancyTenantCommandType {
        CREATE_TENANCY,
        UPDATE_TENANCY,
        CLOSE_TENANCY,
        CONFIRM_TENANCY,
        CANCEL_TENANCY_CLOSURE,
    }

    // Defines the commands that the Provider may perform on a Tenancy
    public enum TenancyProviderCommandType {
        SUSPEND_TENANCY,
        RELEASE_TENANCY,
        STOP_TENANCY
    }

    @BsonProperty("_id")
    private ObjectId commandId;

    private Instant timeStamp;
    private String originatingUser;

    public TenancyCommand(String userId) {
        this.commandId = new ObjectId();
        this.originatingUser = userId;
        this.timeStamp = Instant.now();
    }

    public @BsonProperty("_id") ObjectId getCommandId() { return this.commandId; }
    public String getUserId() {
        return this.originatingUser;
    }

    public Instant getTimeStamp() {
        return this.timeStamp;
    }

}
