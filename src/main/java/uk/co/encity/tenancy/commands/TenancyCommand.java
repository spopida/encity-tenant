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
        @Deprecated CHANGE_PORTFOLIO,
        @Deprecated CHANGE_HMRC_AGENT_VAT_ENABLEMENT,
        @Deprecated REQUEST_HMRC_AGENT_VAT_AUTHORISATION,
        @Deprecated  AUTHORISE_HMRC_AGENT_VAT,
        REQUEST_HMRC_VAT_AUTHZ,
        REJECT_HMRC_VAT_AUTHZ,
        CONFIRM_HMRC_VAT_AUTHZ,
        CHANGE_PORTFOLIO_MEMBER_VAT_ENABLEMENT,
        CHANGE_PORTFOLIO_MEMBER_VAT_REG_NO,
        CHANGE_PORTFOLIO_MEMBER_DIRECT_AUTH,
        CHANGE_PORTFOLIO_MEMBER_DIRECT_CONTACT,
        ADD_PORTFOLIO_MEMBER,
        DELETE_PORTFOLIO_MEMBER
    }

    protected static final Map<String, TenancyTenantCommandType> ACTION_MAP;

    static {
        ACTION_MAP = new HashMap<String, TenancyTenantCommandType>();
        ACTION_MAP.put("confirm", TenancyTenantCommandType.valueOf("CONFIRM_TENANCY"));
        ACTION_MAP.put("reject", TenancyTenantCommandType.valueOf("REJECT_TENANCY"));
        ACTION_MAP.put("change_portfolio_member_vat_enabled", TenancyTenantCommandType.valueOf("CHANGE_PORTFOLIO_MEMBER_VAT_ENABLEMENT"));
        ACTION_MAP.put("change_portfolio_member_vat_reg_no", TenancyTenantCommandType.valueOf("CHANGE_PORTFOLIO_MEMBER_VAT_REG_NO"));
        ACTION_MAP.put("change_portfolio_member_direct_auth", TenancyTenantCommandType.valueOf("CHANGE_PORTFOLIO_MEMBER_DIRECT_AUTH"));
        ACTION_MAP.put("change_portfolio_member_direct_contact", TenancyTenantCommandType.valueOf("CHANGE_PORTFOLIO_MEMBER_DIRECT_CONTACT"));
        ACTION_MAP.put("add_portfolio_member", TenancyTenantCommandType.valueOf("ADD_PORTFOLIO_MEMBER"));
        ACTION_MAP.put("delete_portfolio_member", TenancyTenantCommandType.valueOf("DELETE_PORTFOLIO_MEMBER"));
        ACTION_MAP.put( "request_hmrc_vat_authz", TenancyTenantCommandType.valueOf("REQUEST_HMRC_VAT_AUTHZ"));
        ACTION_MAP.put("reject_hmrc_vat_authz", TenancyTenantCommandType.valueOf("REJECT_HMRC_VAT_AUTHZ"));
        ACTION_MAP.put("confirm_hmrc_vat_authz", TenancyTenantCommandType.valueOf("CONFIRM_HMRC_VAT_AUTHZ"));

        // Deprecated - assuming we get rid of agent actions
        ACTION_MAP.put( "change_portfolio", TenancyTenantCommandType.valueOf("CHANGE_PORTFOLIO"));
        ACTION_MAP.put("change_hmrc_agent_vat_enablement", TenancyTenantCommandType.valueOf("CHANGE_HMRC_AGENT_VAT_ENABLEMENT"));
        ACTION_MAP.put("request_hmrc_agent_vat_authorisation", TenancyTenantCommandType.valueOf("REQUEST_HMRC_AGENT_VAT_AUTHORISATION"));
        ACTION_MAP.put("authorise_hmrc_agent_vat", TenancyTenantCommandType.valueOf("AUTHORISE_HMRC_AGENT_VAT"));

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
