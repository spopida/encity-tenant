package uk.co.encity.tenant;

import org.json.JSONObject;

/**
 * Interface for a repository that can handle tenancy-related commands, events, and entities
 */
public interface ITenancyRepository {
    public void captureTenantCommand(TenancyCommand.TenancyTenantCommandType commandType, TenancyCommand command);
    public void captureProviderCommand(TenancyCommand.TenancyProviderCommandType commandType, JSONObject command);
    public void captureEvent(Tenancy.TenancyEventType eventType, TenancyCreatedEvent event);
    public Tenancy getTenancy(String id);
    public boolean tenancyExists(String id);
}
