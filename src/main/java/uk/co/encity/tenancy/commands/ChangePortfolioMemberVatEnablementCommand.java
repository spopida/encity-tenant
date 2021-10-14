package uk.co.encity.tenancy.commands;

import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.TenancyAvailabilityStatus;
import uk.co.encity.tenancy.events.TenancyEvent;
import uk.co.encity.tenancy.events.PortfolioMemberVatEnablementChangedEvent;

public class ChangePortfolioMemberVatEnablementCommand extends PatchTenancyCommand {
    private boolean vatEnabled;
    private String companyId;

    public ChangePortfolioMemberVatEnablementCommand(String hexTenancyId, String companyId, boolean vatEnabled) {
        super(TenancyTenantCommandType.CHANGE_PORTFOLIO_MEMBER_VAT_ENABLEMENT, hexTenancyId);
        this.vatEnabled = vatEnabled;
        this.companyId = companyId;
    }

    @Override
    public void checkPreConditions(Tenancy t) throws PreConditionException {
        if (t.getAvailabilityStatus() != TenancyAvailabilityStatus.OPERATIONAL) {
            throw new PreConditionException( String.format( "Cannot alter tenancy %s since it is not operational", t.getHexTenancyId()));
        }
    }

    @Override
    public TenancyEvent createTenancyEvent(Tenancy t) {
        return new PortfolioMemberVatEnablementChangedEvent( this, t, this.companyId, this.vatEnabled);
    }
}
