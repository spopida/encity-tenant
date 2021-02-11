package uk.co.encity.tenancy.commands;

import org.springframework.lang.NonNull;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.events.TenancyEvent;

public abstract class PatchTenancyCommand extends TenancyCommand {

    private TenancyCommand.TenancyTenantCommandType cmdType;
    private String hexTenancyId;

    public PatchTenancyCommand(TenancyCommand.TenancyTenantCommandType cmdType, String hexTenancyId) {
        this.hexTenancyId = hexTenancyId;
        this.cmdType = cmdType;
    }

    public TenancyTenantCommandType getCommandType() { return this.cmdType; }
    public String getHexTenancyId() { return this.hexTenancyId; }

    public abstract void checkPreConditions(Tenancy t) throws PreConditionException;
    public abstract TenancyEvent createTenancyEvent(Tenancy t);

    public static PatchTenancyCommand getPatchTenancyCommand(
        @NonNull TenancyCommand.TenancyTenantCommandType cmdtype,
        String hexTenancyId)
    {
        PatchTenancyCommand patchCmd = null;

        switch (cmdtype) {
            case CONFIRM_TENANCY:
                patchCmd = new ConfirmTenancyCommand(hexTenancyId);
                break;
            case REJECT_TENANCY:
                patchCmd = new RejectTenancyCommand(hexTenancyId);
                break;
        }

        return patchCmd;
    }
}
