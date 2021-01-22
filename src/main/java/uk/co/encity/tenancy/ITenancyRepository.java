package uk.co.encity.tenancy;

import org.json.JSONObject;

/**
 * Interface for a repository that can handle tenancy-related commands, events, and entities
 */
public interface ITenancyRepository {
    public void captureTenantCommand(TenancyCommand.TenancyTenantCommandType commandType, TenancyCommand command);
    public void captureProviderCommand(TenancyCommand.TenancyProviderCommandType commandType, JSONObject command);

    // Create a Tenancy, passing in the command.  Just create a Tenancy with an Id and a reference to the creation event

    public void captureTenancySnapshot(TenancyCreatedEvent evt);
    // TODO: public void captureTenancySnapshot(TenancyEvent fromEvent, TenancyEvent toEvent);
    public void captureEvent(TenancySnapshot.TenancyEventType eventType, TenancyCreatedEvent event);
    public TenancySnapshot getTenancy(String id);
    public boolean tenancyExists(String id);
}
