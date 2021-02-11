package uk.co.encity.tenancy.commands;

import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.TenancyProviderStatus;
import uk.co.encity.tenancy.entity.TenancyTenantStatus;
import uk.co.encity.tenancy.events.TenancyConfirmedEvent;
import uk.co.encity.tenancy.events.TenancyEvent;

public class ConfirmTenancyCommand extends PatchTenancyCommand {

    // TODO - include the TenancyId here

    public ConfirmTenancyCommand(String hexTenancyId) {
        super(TenancyTenantCommandType.CONFIRM_TENANCY, hexTenancyId);
    }

    @Override
    public void checkPreConditions(Tenancy t) throws PreConditionException {
        if (t.getTenantStatus() != TenancyTenantStatus.UNCONFIRMED) {
            throw new PreConditionException(
                "Cannot confirm tenancy " + t.getHexTenancyId() + " due to failed pre-condition");
        }
        if (t.getProviderStatus() != TenancyProviderStatus.ACTIVE) {
            throw new PreConditionException(
                "Cannot confirm tenancy " + t.getHexTenancyId() + " due to failed pre-condition");
        }
        return;
    }

    @Override
    public TenancyEvent createTenancyEvent(Tenancy t) {
        return new TenancyConfirmedEvent(this, t);
    }
}
