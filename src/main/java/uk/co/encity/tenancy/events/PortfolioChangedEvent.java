package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.databind.module.SimpleModule;
import uk.co.encity.tenancy.commands.PatchTenancyCommand;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.VatSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public class PortfolioChangedEvent extends TenancyEvent {

    private List<String> newPortfolio;
    private Map<String, VatSettings> newPortfolioDetails;

    public PortfolioChangedEvent() {};

    /*
    public void setNewPortfolio(
            List<String> newPortfolio,
            Map<String, VatSettings> newPortfolioDetails) {
        this.newPortfolio = newPortfolio;

        // Guarantee that the map has a single entry for each item in the newPortfolio array
        this.newPortfolioDetails = new HashMap<String, VatSettings>();

        newPortfolio.forEach(companyNumber -> {
            VatSettings vatSettings = newPortfolioDetails.get(companyNumber);
            if (vatSettings == null) vatSettings = new VatSettings();
            this.newPortfolioDetails.put(companyNumber, vatSettings);
        });

        return;
    }
    */

    public void setNewPortfolio(List<String> newPortfolio) {
        this.newPortfolio = newPortfolio;
    }

    public void setNewPortfolioDetails(Map<String, VatSettings> newPortfolioDetails) {
        this.newPortfolioDetails = newPortfolioDetails;
    }
    /**
     * Construct an event representing tenancy confirmation, using information
     * from the command that led to the event, and the pre-event version of the
     * affected tenancy
     * @param cmd the command that caused the event
     * @param current the pre-event version of the tenancy
     */
    public PortfolioChangedEvent(
            PatchTenancyCommand cmd,
            Tenancy current,
            List<String> newPortfolio,
            Map<String, VatSettings> newPortfolioDetails) {
        super(
                TenancyEventType.PORTFOLIO_CHANGED,
                current.getTenancyId(),
                current.getVersion() + 1,
                cmd.getCommandId());

        //this.setNewPortfolio(newPortfolio, newPortfolioDetails);
        this.setNewPortfolio(newPortfolio);
        this.setNewPortfolioDetails(newPortfolioDetails);
    }

    public List<String> getNewPortfolio() {
        return newPortfolio;
    }
    public Map<String, VatSettings> getNewPortfolioDetails() { return this.newPortfolioDetails; }

    @Override
    public Tenancy applyToTenancy(Tenancy target) {
        //target.setDefaultPortfolio(this.newPortfolio);
        target.setPortfolioDetails(this.newPortfolioDetails);
        return target;
    }

    @Override
    public void addSerializerToModule(SimpleModule module) {
        module.addSerializer(this.getClass(), new PortfolioChangedEventSerializer());
    }

    @Override
    public String getRoutingKey() { return "encity.tenancy.portfolio_changed"; }
}
