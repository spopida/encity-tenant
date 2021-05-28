package uk.co.encity.tenancy.commands;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import uk.co.encity.tenancy.entity.Tenancy;

public abstract class TenancyCommand {

    // Defines the commands that a Tenant may perform on a Tenancy
    public enum TenancyTenantCommandType {
        CREATE_TENANCY,
        UPDATE_TENANCY,
        CLOSE_TENANCY,
        CONFIRM_TENANCY,
        REJECT_TENANCY,
        CANCEL_TENANCY_CLOSURE,
        CHANGE_PORTFOLIO
    }

    protected static final Map<String, TenancyTenantCommandType> ACTION_MAP;

    static {
        ACTION_MAP = new HashMap<String, TenancyTenantCommandType>();
        ACTION_MAP.put("confirm", TenancyTenantCommandType.valueOf("CONFIRM_TENANCY"));
        ACTION_MAP.put("reject", TenancyTenantCommandType.valueOf("REJECT_TENANCY"));
        ACTION_MAP.put( "change_portfolio", TenancyTenantCommandType.valueOf("CHANGE_PORTFOLIO"));
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

    public TenancyCommand() {
        this.commandId = new ObjectId();
        this.timeStamp = Instant.now();
    }

    public @BsonProperty("_id") ObjectId getCommandId() { return this.commandId; }
    public Instant getTimeStamp() {
        return this.timeStamp;
    }

}
