package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.databind.module.SimpleModule;
import uk.co.encity.tenancy.commands.PatchTenancyCommand;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.VatSettings;

public class PortfolioMemberDirectAuthChangedEvent extends TenancyEvent {

    private boolean directAuth;
    private String companyId;

    public PortfolioMemberDirectAuthChangedEvent() {};

    public PortfolioMemberDirectAuthChangedEvent(
            PatchTenancyCommand cmd,
            Tenancy current,
            String companyId,
            boolean directAuth) {
        super(
                TenancyEventType.PORTFOLIO_MEMBER_DIRECT_AUTH_CHANGED,
                current.getTenancyId(),
                current.getVersion() + 1,
                cmd.getCommandId());
        this.setDirectAuth(directAuth);
        this.setCompanyId(companyId);
    }

    public boolean getDirectAuth() { return this.directAuth; }
    public boolean isDirectAuth() { return this.getDirectAuth();}
    public void setDirectAuth(boolean value) { this.directAuth = value; }

    public String getCompanyId() { return this.companyId; }
    public void setCompanyId(String value) { this.companyId = value; }

    @Override
    public Tenancy applyToTenancy(Tenancy target) {
        // Get the portfolio member using this.companyId, then set the value!
        VatSettings memberDetails = target.getPortfolioDetails().get(this.companyId);
        if (memberDetails == null) {
            // This is unexpected - perhaps we ought to throw instead of...
            VatSettings details = new VatSettings();
            details.setDirectAuthorisation(this.directAuth);
            target.getPortfolioDetails().put(this.companyId, details);
        } else {
            memberDetails.setDirectAuthorisation(this.directAuth);
        }
        return target;
    }

    @Override
    public void addSerializerToModule(SimpleModule module) {
        module.addSerializer(this.getClass(), new PortfolioMemberDirectAuthChangedEventSerializer());
    }

    @Override
    public String getRoutingKey() { return "encity.tenancy.portfolio_member_vat_enablement_changed"; }
}
