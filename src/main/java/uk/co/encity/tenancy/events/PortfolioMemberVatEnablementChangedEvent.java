package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.databind.module.SimpleModule;

import uk.co.encity.tenancy.commands.PatchTenancyCommand;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.VatSettings;

public class PortfolioMemberVatEnablementChangedEvent extends TenancyEvent {

    private boolean vatEnabled;
    private String companyId;

    public PortfolioMemberVatEnablementChangedEvent() {};

    public PortfolioMemberVatEnablementChangedEvent(
            PatchTenancyCommand cmd,
            Tenancy current,
            String companyId,
            boolean vatEnabled) {
        super(
                TenancyEventType.PORTFOLIO_MEMBER_VAT_ENABLEMENT_CHANGED,
                current.getTenancyId(),
                current.getVersion() + 1,
                cmd.getCommandId());
        this.setVatEnabled(vatEnabled);
        this.setCompanyId(companyId);
    }

    public boolean getVatEnabled() { return this.vatEnabled; }
    public boolean isVatEnabled() { return this.getVatEnabled();}
    public void setVatEnabled(boolean value) { this.vatEnabled = value; }

    public String getCompanyId() { return this.companyId; }
    public void setCompanyId(String value) { this.companyId = value; }

    @Override
    public Tenancy applyToTenancy(Tenancy target) {
        // Get the portfolio member using this.companyId, then set the value!
        VatSettings memberDetails = target.getPortfolioDetails().get(this.companyId);
        if (memberDetails == null) {
            // This is unexpected - perhaps we ought to throw instead of...
            VatSettings details = new VatSettings();
            details.setVatEnabled(this.vatEnabled);
            target.getPortfolioDetails().put(this.companyId, details);
        } else {
            memberDetails.setVatEnabled(this.vatEnabled);
        }
        return target;
    }

    @Override
    public void addSerializerToModule(SimpleModule module) {
        module.addSerializer(this.getClass(), new PortfolioMemberVatEnablementChangedEventSerializer());
    }

    @Override
    public String getRoutingKey() { return "encity.tenancy.portfolio_member_vat_enablement_changed"; }
}
