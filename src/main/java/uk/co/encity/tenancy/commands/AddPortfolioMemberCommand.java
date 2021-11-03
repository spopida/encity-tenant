package uk.co.encity.tenancy.commands;

import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.TenancyAvailabilityStatus;
import uk.co.encity.tenancy.events.PortfolioMemberAddedEvent;
import uk.co.encity.tenancy.events.TenancyEvent;

public class AddPortfolioMemberCommand extends PatchTenancyCommand {
    private String companyId;
    private boolean vatEnabled;
    private String vatRegNo;
    private boolean directAuthorisation;
    private String directContactEmail;

    public AddPortfolioMemberCommand(
            String hexTenancyId,
            String companyId,
            boolean vatEnabled,
            String vatRegNo,
            boolean directAuthorisation,
            String directContactEmail)
    {
        super(TenancyTenantCommandType.ADD_PORTFOLIO_MEMBER, hexTenancyId);
        this.companyId = companyId;
        this.vatEnabled = vatEnabled;
        this.vatRegNo = vatRegNo;
        this.directAuthorisation = directAuthorisation;
        this.directContactEmail = directContactEmail;
    }

    @Override
    public void checkPreConditions(Tenancy t) throws PreConditionException {
        if (t.getAvailabilityStatus() != TenancyAvailabilityStatus.OPERATIONAL) {
            throw new PreConditionException( String.format( "Cannot alter tenancy %s since it is not operational", t.getHexTenancyId()));
        }
    }

    @Override
    public TenancyEvent createTenancyEvent(Tenancy t) {
        return new PortfolioMemberAddedEvent(
                this,
                t,
                this.companyId,
                this.vatEnabled,
                this.vatRegNo,
                this.directAuthorisation,
                this.directContactEmail);
    }
}
