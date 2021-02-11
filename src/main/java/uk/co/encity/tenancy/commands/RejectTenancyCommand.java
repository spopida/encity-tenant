package uk.co.encity.tenancy.commands;

import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.TenancyProviderStatus;
import uk.co.encity.tenancy.entity.TenancyTenantStatus;
import uk.co.encity.tenancy.events.TenancyRejectedEvent;
import uk.co.encity.tenancy.events.TenancyEvent;

public class RejectTenancyCommand extends PatchTenancyCommand {

    public RejectTenancyCommand(String hexTenancyId) {
        super(TenancyTenantCommandType.REJECT_TENANCY,
        hexTenancyId);
    }

    @Override
    public void checkPreConditions(Tenancy t) throws PreConditionException {
        if (t.getTenantStatus() != TenancyTenantStatus.UNCONFIRMED) {
            throw new PreConditionException(
                "Cannot reject tenancy " + t.getHexTenancyId() + " due to failed pre-condition");
        }
        if (t.getProviderStatus() != TenancyProviderStatus.ACTIVE) {
            throw new PreConditionException(
                    "Cannot reject tenancy " + t.getHexTenancyId() + " due to failed pre-condition");
        }
        return;
    }

    @Override
    public TenancyEvent createTenancyEvent(Tenancy t) {
        return new TenancyRejectedEvent(this, t);
    }


}
