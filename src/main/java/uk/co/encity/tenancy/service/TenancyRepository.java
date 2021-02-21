package uk.co.encity.tenancy.service;

import org.json.JSONObject;
import uk.co.encity.tenancy.commands.TenancyCommand;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.events.TenancyCreatedEvent;
import uk.co.encity.tenancy.events.TenancyEvent;
import uk.co.encity.tenancy.events.TenancyEventType;
import uk.co.encity.tenancy.snapshot.TenancySnapshot;

import java.io.IOException;
import java.util.List;

/**
 * Interface for a repository that can handle tenancy-related commands, events, and entities
 */
public interface TenancyRepository {
    public void captureTenancyCommand(TenancyCommand.TenancyTenantCommandType commandType, TenancyCommand command);
    public void captureProviderCommand(TenancyCommand.TenancyProviderCommandType commandType, JSONObject command);
    public void captureTenancySnapshot(TenancyCreatedEvent evt);
    // TODO: public void captureTenancySnapshot(TenancyEvent fromEvent, TenancyEvent toEvent);
    public void captureEvent(TenancyEventType eventType, TenancyEvent event);
    public Tenancy getTenancy(String id) throws IOException;
    public Tenancy getTenancyFromDomain(String domain) throws IOException;
    public boolean tenancyExists(String id);
    public TenancySnapshot getLatestSnapshot(String id);
    public TenancySnapshot getLatestSnapshot(String fieldName, String value);
    public List<TenancyEvent> getEventRange(String tenancyId, int fromVersion);
}
