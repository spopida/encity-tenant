package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.databind.module.SimpleModule;
import uk.co.encity.tenancy.commands.PatchTenancyCommand;
import uk.co.encity.tenancy.entity.Tenancy;

import java.util.ArrayList;
import java.util.List;

public class PortfolioChangedEvent extends TenancyEvent {

    private List<String> newPortfolio;

    public PortfolioChangedEvent() {};

    public void setNewPortfolio(ArrayList<String> newPortfolio) {
        this.newPortfolio = newPortfolio;
    }
    /**
     * Construct an event representing tenancy confirmation, using information
     * from the command that led to the event, and the pre-event version of the
     * affected tenancy
     * @param cmd the command that caused the event
     * @param current the pre-event version of the tenancy
     */
    public PortfolioChangedEvent(PatchTenancyCommand cmd, Tenancy current, List<String> newPortfolio) {
        super(
                TenancyEventType.PORTFOLIO_CHANGED,
                current.getTenancyId(),
                current.getVersion() + 1,
                cmd.getCommandId());

        this.newPortfolio = newPortfolio;
    }

    public List<String> getNewPortfolio() {
        return newPortfolio;
    }

    @Override
    public Tenancy applyToTenancy(Tenancy target) {
        target.setDefaultPortfolio(newPortfolio);
        return target;
    }

    @Override
    public void addSerializerToModule(SimpleModule module) {
        module.addSerializer(this.getClass(), new PortfolioChangedEventSerializer());
    }

    @Override
    public String getRoutingKey() { return "encity.tenancy.portfolio_changed"; }
}
