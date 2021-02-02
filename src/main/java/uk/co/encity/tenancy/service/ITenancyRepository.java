package uk.co.encity.tenancy.service;

import org.json.JSONObject;
import uk.co.encity.tenancy.commands.TenancyCommand;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.events.TenancyCreatedEvent;
import uk.co.encity.tenancy.events.TenancyEvent;
import uk.co.encity.tenancy.events.TenancyEventType;
import uk.co.encity.tenancy.snapshot.TenancySnapshot;

import java.util.List;

/**
 * Interface for a repository that can handle tenancy-related commands, events, and entities
 */
public interface ITenancyRepository {
    public void captureTenantCommand(TenancyCommand.TenancyTenantCommandType commandType, TenancyCommand command);
    public void captureProviderCommand(TenancyCommand.TenancyProviderCommandType commandType, JSONObject command);

    // Create a Tenancy, passing in the command.  Just create a Tenancy with an Id and a reference to the creation event

    public void captureTenancySnapshot(TenancyCreatedEvent evt);
    // TODO: public void captureTenancySnapshot(TenancyEvent fromEvent, TenancyEvent toEvent);
    public void captureEvent(TenancyEventType eventType, TenancyCreatedEvent event);
    public Tenancy getTenancy(String id);
    public boolean tenancyExists(String id);
    public TenancySnapshot getLatestSnapshot(String id);
    public List<TenancyEvent> getEventRange(int fromVersion);
}
