package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.databind.module.SimpleModule;
import uk.co.encity.tenancy.commands.PatchTenancyCommand;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.VatSettings;

public class PortfolioMemberDirectContactChangedEvent extends TenancyEvent {

    private String directContact;
    private String companyId;

    public PortfolioMemberDirectContactChangedEvent() {};

    public PortfolioMemberDirectContactChangedEvent(
            PatchTenancyCommand cmd,
            Tenancy current,
            String companyId,
            String directContact) {
        super(
                TenancyEventType.PORTFOLIO_MEMBER_DIRECT_CONTACT_CHANGED,
                current.getTenancyId(),
                current.getVersion() + 1,
                cmd.getCommandId());
        this.setDirectContact(directContact);
        this.setCompanyId(companyId);
    }

    public String getDirectContact() { return this.directContact; }
    public void setDirectContact(String value) { this.directContact = value; }

    public String getCompanyId() { return this.companyId; }
    public void setCompanyId(String value) { this.companyId = value; }

    @Override
    public Tenancy applyToTenancy(Tenancy target) {
        // Get the portfolio member using this.companyId, then set the value!
        VatSettings memberDetails = target.getPortfolioDetails().get(this.companyId);
        if (memberDetails == null) {
            // This is unexpected - perhaps we ought to throw instead of...
            VatSettings details = new VatSettings();
            details.setDirectContactEmail(this.directContact);
            target.getPortfolioDetails().put(this.companyId, details);
        } else {
            memberDetails.setDirectContactEmail(this.directContact);
        }
        return target;
    }

    @Override
    public void addSerializerToModule(SimpleModule module) {
        module.addSerializer(this.getClass(), new PortfolioMemberDirectContactChangedEventSerializer());
    }

    @Override
    public String getRoutingKey() { return "encity.tenancy.portfolio_member_vat_reg_no_changed"; }
}
