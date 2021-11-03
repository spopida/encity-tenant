package uk.co.encity.tenancy.commands;

import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.TenancyAvailabilityStatus;
import uk.co.encity.tenancy.events.PortfolioMemberDeletedEvent;
import uk.co.encity.tenancy.events.TenancyEvent;

public class DeletePortfolioMemberCommand extends PatchTenancyCommand {
    private String companyId;

    public DeletePortfolioMemberCommand(String hexTenancyId, String companyId) {
        super(TenancyTenantCommandType.DELETE_PORTFOLIO_MEMBER, hexTenancyId);
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
        return new PortfolioMemberDeletedEvent(this, t, this.companyId);
    }
}
