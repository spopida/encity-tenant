package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.databind.module.SimpleModule;
import uk.co.encity.tenancy.commands.PatchTenancyCommand;
import uk.co.encity.tenancy.components.TenancyContact;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.TenancyTenantStatus;

/**
 * An event that represents the tenant-originated confirmation of an unconfirmed
 * new tenancy
 */
public class TenancyConfirmedEvent extends TenancyEvent {

    /**
     * The admin user that should be set up upon confirmation of the tenancy
     */
    private TenancyContact originalAdminUser;

    /**
     * The domain of the confirmed tenancy
     */
    private String domain;

    public TenancyConfirmedEvent() {}

    /**
     * Construct an event representing tenancy confirmation, using information
     * from the command that led to the event, and the pre-event version of the
     * affected tenancy
     * @param cmd the command that caused the event
     * @param current the pre-event version of the tenancy
     */
    public TenancyConfirmedEvent(PatchTenancyCommand cmd, Tenancy current) {
        super(
                TenancyEventType.TENANCY_CONFIRMED,
                current.getTenancyId(),
                current.getVersion() + 1,
                cmd.getCommandId());

        this.originalAdminUser = current.getOriginalAdminUser();
        this.domain = current.getDomain();
    }

    public TenancyContact getOriginalAdminUser() { return this.originalAdminUser; }
    public String getDomain() { return this.domain; }

    @Override
    public Tenancy applyToTenancy(Tenancy target) {
        target.setTenantStatus(TenancyTenantStatus.CONFIRMED);
        return target;
    }

    @Override
    public void addSerializerToModule(SimpleModule module) {
        module.addSerializer(this.getClass(), new TenancyConfirmedEventSerializer());
    }

    @Override
    public String getRoutingKey() { return "encity.tenancy.confirmed"; }
}
