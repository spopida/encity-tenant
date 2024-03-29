package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.databind.module.SimpleModule;
import uk.co.encity.tenancy.commands.PatchTenancyCommand;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.TenancyTenantStatus;

public class TenancyRejectedEvent extends TenancyEvent {

    public TenancyRejectedEvent() {}

    public TenancyRejectedEvent(PatchTenancyCommand cmd, Tenancy current) {
        super(
                TenancyEventType.TENANCY_REJECTED,
                current.getTenancyId(),
                current.getVersion() + 1,
                cmd.getCommandId());
    }

    @Override
    public Tenancy applyToTenancy(Tenancy target) {
        target.setTenantStatus(TenancyTenantStatus.REJECTED);
        return target;
    }

    @Override
    public void addSerializerToModule(SimpleModule module) {
        module.addSerializer(this.getClass(), new TenancyRejectedEventSerializer());
    }

    @Override
    public String getRoutingKey() { return "encity.tenancy.rejected"; }
}
