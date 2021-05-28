package uk.co.encity.tenancy.commands;

import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.TenancyAvailabilityStatus;
import uk.co.encity.tenancy.entity.TenancyProviderStatus;
import uk.co.encity.tenancy.entity.TenancyTenantStatus;
import uk.co.encity.tenancy.events.PortfolioChangedEvent;
import uk.co.encity.tenancy.events.TenancyEvent;

import java.util.List;

public class ChangePortfolioCommand extends PatchTenancyCommand {

    private List<String> entityIds;

    public ChangePortfolioCommand(String hexTenancyId, List<String> entityIds) {
        super(TenancyTenantCommandType.CHANGE_PORTFOLIO, hexTenancyId);
        this.entityIds = entityIds;
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
        return new PortfolioChangedEvent(this, t, entityIds);
    }

    public List<String> getEntityIds() {
        return entityIds;
    }
}
