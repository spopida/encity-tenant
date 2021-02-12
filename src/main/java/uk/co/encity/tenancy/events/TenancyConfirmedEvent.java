package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.databind.module.SimpleModule;
import uk.co.encity.tenancy.commands.PatchTenancyCommand;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.TenancyTenantStatus;

public class TenancyConfirmedEvent extends TenancyEvent {

    public TenancyConfirmedEvent() {}

    public TenancyConfirmedEvent(PatchTenancyCommand cmd, Tenancy current) {
        super(
                TenancyEventType.TENANCY_CONFIRMED,
                current.getTenancyId(),
                current.getVersion() + 1,
                cmd.getCommandId());
    }

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
