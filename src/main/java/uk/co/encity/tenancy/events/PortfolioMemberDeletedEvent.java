package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.Getter;
import lombok.Setter;
import uk.co.encity.tenancy.commands.PatchTenancyCommand;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.VatSettings;

import java.util.Map;

@Getter
@Setter
public class PortfolioMemberDeletedEvent extends TenancyEvent {
    private String companyId;

    public PortfolioMemberDeletedEvent() {};

    public PortfolioMemberDeletedEvent(
            PatchTenancyCommand cmd,
            Tenancy current,
            String companyId
    ) {
        super(
                TenancyEventType.PORTFOLIO_MEMBER_DIRECT_CONTACT_CHANGED,
                current.getTenancyId(),
                current.getVersion() + 1,
                cmd.getCommandId());
        this.setCompanyId(companyId);
    }

    @Override
    public Tenancy applyToTenancy(Tenancy target) {
        // Get the portfolio details map, and remove the entry with this.companyId
        Map<String, VatSettings> thePortfolio = target.getPortfolioDetails();

        thePortfolio.remove(this.companyId);
        return target;
    }

    @Override
    public void addSerializerToModule(SimpleModule module) {
        module.addSerializer(this.getClass(), new PortfolioMemberDeletedEventSerializer());
    }

    @Override
    public String getRoutingKey() { return "encity.tenancy.portfolio_member_deleted"; }
}
