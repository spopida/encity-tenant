package uk.co.encity.tenancy.commands;

import lombok.Getter;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.TenancyAvailabilityStatus;
import uk.co.encity.tenancy.events.HmrcVatEnablementChangedEvent;
import uk.co.encity.tenancy.events.TenancyEvent;

@Getter
public class ChangeHmrcAgentVatEnablementCommand extends PatchTenancyCommand {
    private boolean hmrcVatEnabled;

    public ChangeHmrcAgentVatEnablementCommand(String hexTenancyId, boolean hmrcVatEnabled) {
        super(TenancyCommand.TenancyTenantCommandType.CHANGE_HMRC_AGENT_VAT_ENABLEMENT, hexTenancyId);
        this.hmrcVatEnabled = hmrcVatEnabled;
    }

    @Override
    public void checkPreConditions(Tenancy t) throws PreConditionException {
        if (t.getAvailabilityStatus() != TenancyAvailabilityStatus.OPERATIONAL) {
            throw new PreConditionException( String.format( "Cannot alter tenancy %s since it is not operational", t.getHexTenancyId()));
        }
    }

    @Override
    public TenancyEvent createTenancyEvent(Tenancy t) {
        return new HmrcVatEnablementChangedEvent(this, t, this.hmrcVatEnabled);
    }
}
