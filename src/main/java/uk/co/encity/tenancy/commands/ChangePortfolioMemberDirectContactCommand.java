package uk.co.encity.tenancy.commands;

import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.TenancyAvailabilityStatus;
import uk.co.encity.tenancy.events.PortfolioMemberDirectContactChangedEvent;
import uk.co.encity.tenancy.events.TenancyEvent;

public class ChangePortfolioMemberDirectContactCommand extends PatchTenancyCommand {
    private String directContact;
    private String companyId;

    public ChangePortfolioMemberDirectContactCommand(String hexTenancyId, String companyId, String directContact) {
        super(TenancyTenantCommandType.CHANGE_PORTFOLIO_MEMBER_DIRECT_CONTACT, hexTenancyId);
        this.directContact = directContact;
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
        return new PortfolioMemberDirectContactChangedEvent( this, t, this.companyId, this.directContact);
    }
}
