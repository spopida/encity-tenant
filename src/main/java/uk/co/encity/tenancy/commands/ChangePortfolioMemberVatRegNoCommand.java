package uk.co.encity.tenancy.commands;

import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.TenancyAvailabilityStatus;
import uk.co.encity.tenancy.events.PortfolioMemberVatEnablementChangedEvent;
import uk.co.encity.tenancy.events.PortfolioMemberVatRegNoChangedEvent;
import uk.co.encity.tenancy.events.TenancyEvent;

public class ChangePortfolioMemberVatRegNoCommand extends PatchTenancyCommand {
    private String vatRegNo;
    private String companyId;

    public ChangePortfolioMemberVatRegNoCommand(String hexTenancyId, String companyId, String vatRegNo) {
        super(TenancyTenantCommandType.CHANGE_PORTFOLIO_MEMBER_VAT_REG_NO, hexTenancyId);
        this.vatRegNo = vatRegNo;
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
        return new PortfolioMemberVatRegNoChangedEvent( this, t, this.companyId, this.vatRegNo);
    }
}