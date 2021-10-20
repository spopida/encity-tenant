package uk.co.encity.tenancy.commands;

import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.TenancyAvailabilityStatus;
import uk.co.encity.tenancy.events.PortfolioMemberDirectAuthChangedEvent;
import uk.co.encity.tenancy.events.TenancyEvent;

public class ChangePortfolioMemberDirectAuthCommand extends PatchTenancyCommand {
    private boolean directAuth;
    private String companyId;

    public ChangePortfolioMemberDirectAuthCommand(String hexTenancyId, String companyId, boolean directAuth) {
        super(TenancyTenantCommandType.CHANGE_PORTFOLIO_MEMBER_VAT_ENABLEMENT, hexTenancyId);
        this.directAuth = directAuth;
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
        return new PortfolioMemberDirectAuthChangedEvent( this, t, this.companyId, this.directAuth);
    }
}
