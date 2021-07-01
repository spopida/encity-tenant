package uk.co.encity.tenancy.commands;

import lombok.Getter;
import lombok.Setter;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.TenancyAvailabilityStatus;
import uk.co.encity.tenancy.entity.VatSettings;
import uk.co.encity.tenancy.events.PortfolioChangedEvent;
import uk.co.encity.tenancy.events.TenancyEvent;

import java.util.List;
import java.util.Map;

@Getter
public class ChangePortfolioCommand extends PatchTenancyCommand {

    private List<String> entityIds;

    /**
     * A map of VAT Settings for the tenancy, keyed on Company Number.  This may need to expand to encompass other
     * settings (e.g. Corp Tax, PAYE, NEST, ...) - but we're some way off that so stick to YAGNI for now
     */
    private Map<String, VatSettings> portfolioDetails;

    public ChangePortfolioCommand(String hexTenancyId, List<String> entityIds, Map<String, VatSettings> portfolioDetails) {
        super(TenancyTenantCommandType.CHANGE_PORTFOLIO, hexTenancyId);
        this.entityIds = entityIds;
        this.portfolioDetails = portfolioDetails;
    }

    @Override
    public void checkPreConditions(Tenancy t) throws PreConditionException {
        if (t.getAvailabilityStatus() != TenancyAvailabilityStatus.OPERATIONAL) {
            throw new PreConditionException( String.format( "Cannot alter tenancy %s since it is not operational", t.getHexTenancyId()));
        }
        return;
    }

    @Override
    public TenancyEvent createTenancyEvent(Tenancy t) {
        return new PortfolioChangedEvent(this, t, this.entityIds, this.portfolioDetails);
    }
}
