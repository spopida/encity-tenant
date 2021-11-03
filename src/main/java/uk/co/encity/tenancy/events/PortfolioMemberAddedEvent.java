package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.Getter;
import lombok.Setter;
import uk.co.encity.tenancy.commands.PatchTenancyCommand;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.VatSettings;

@Getter
@Setter
public class PortfolioMemberAddedEvent extends TenancyEvent {

    // Need all member attributes for a PM
    private String companyId;
    private boolean vatEnabled;
    private String vatRegNo;
    private boolean directAuthorisation;
    private String directContactEmail;

    public PortfolioMemberAddedEvent() {};

    public PortfolioMemberAddedEvent(
            PatchTenancyCommand cmd,
            Tenancy current,
            String companyId,
            boolean vatEnabled,
            String vatRegNo,
            boolean directAuthorisation,
            String directContactEmail
    ) {
        super(
                TenancyEventType.PORTFOLIO_MEMBER_ADDED,
                current.getTenancyId(),
                current.getVersion() + 1,
                cmd.getCommandId());
        this.setCompanyId(companyId);
        this.setVatEnabled(vatEnabled);
        this.setVatRegNo(vatRegNo);
        this.setDirectAuthorisation(directAuthorisation);
        this.setDirectContactEmail(directContactEmail);
    }

    @Override
    public Tenancy applyToTenancy(Tenancy target) {
        VatSettings memberDetails = target.getPortfolioDetails().get(this.companyId);
        if (memberDetails == null) {
            // This is unexpected - perhaps we ought to throw instead of...
            VatSettings details = new VatSettings();
            details.setVatEnabled(this.isVatEnabled());
            details.setVatRegNo(this.getVatRegNo());
            details.setDirectAuthorisation(this.isDirectAuthorisation());
            details.setDirectContactEmail(this.getDirectContactEmail());
            target.getPortfolioDetails().put(this.companyId, details);
        } else {
            memberDetails.setVatEnabled(this.isVatEnabled());
            memberDetails.setVatRegNo(this.getVatRegNo());
            memberDetails.setDirectAuthorisation(this.isDirectAuthorisation());
            memberDetails.setDirectContactEmail(this.getDirectContactEmail());
        }
        return target;
    }

    @Override
    public void addSerializerToModule(SimpleModule module) {
        module.addSerializer(this.getClass(), new PortfolioMemberAddedEventSerializer());
    }

    @Override
    public String getRoutingKey() { return "encity.tenancy.portfolio_member_added"; }
}
