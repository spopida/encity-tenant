package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.databind.module.SimpleModule;
import uk.co.encity.tenancy.commands.PatchTenancyCommand;
import uk.co.encity.tenancy.entity.Tenancy;
import java.util.UUID;

public class HmrcVatAuthorisationRequestedEvent extends TenancyEvent {

    private UUID requestUUID;

    public HmrcVatAuthorisationRequestedEvent(PatchTenancyCommand cmd, Tenancy current) {
        super(
                TenancyEventType.HMRC_VAT_AUTHORISATION_REQUESTED,
                current.getTenancyId(),
                current.getVersion() + 1,
                cmd.getCommandId());
        this.requestUUID = UUID.randomUUID();
    }

    @Override
    public Tenancy applyToTenancy(Tenancy target) {
        target.setHmrcVatAuthorisationRequestPending(true);
        target.setHmrcVatAuthorisationRequestUUID(this.requestUUID);
        return target;
    }

    @Override
    public void addSerializerToModule(SimpleModule module) {
        // Since this class has no attributes, there's nothing to serialize
        return;
    }

    @Override
    public String getRoutingKey() {
        return "encity.tenancy.hmrc_vat_authorisation_requested";
    }
}
