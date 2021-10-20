package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.databind.module.SimpleModule;
import uk.co.encity.tenancy.commands.PatchTenancyCommand;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.VatSettings;

public class PortfolioMemberVatRegNoChangedEvent extends TenancyEvent {

    private String vatRegNo;
    private String companyId;

    public PortfolioMemberVatRegNoChangedEvent() {};

    public PortfolioMemberVatRegNoChangedEvent(
            PatchTenancyCommand cmd,
            Tenancy current,
            String companyId,
            String vatRegNo) {
        super(
                TenancyEventType.PORTFOLIO_MEMBER_VAT_REG_NO_CHANGED,
                current.getTenancyId(),
                current.getVersion() + 1,
                cmd.getCommandId());
        this.setVatRegNo(vatRegNo);
        this.setCompanyId(companyId);
    }

    public String getVatRegNo() { return this.vatRegNo; }
    public void setVatRegNo(String value) { this.vatRegNo = value; }

    public String getCompanyId() { return this.companyId; }
    public void setCompanyId(String value) { this.companyId = value; }

    @Override
    public Tenancy applyToTenancy(Tenancy target) {
        // Get the portfolio member using this.companyId, then set the value!
        VatSettings memberDetails = target.getPortfolioDetails().get(this.companyId);
        if (memberDetails == null) {
            // This is unexpected - perhaps we ought to throw instead of...
            VatSettings details = new VatSettings();
            details.setVatRegNo(this.vatRegNo);
            target.getPortfolioDetails().put(this.companyId, details);
        } else {
            memberDetails.setVatRegNo(this.vatRegNo);
        }
        return target;
    }

    @Override
    public void addSerializerToModule(SimpleModule module) {
        module.addSerializer(this.getClass(), new PortfolioMemberVatRegNoChangedEventSerializer());
    }

    @Override
    public String getRoutingKey() { return "encity.tenancy.portfolio_member_vat_reg_no_changed"; }
}
