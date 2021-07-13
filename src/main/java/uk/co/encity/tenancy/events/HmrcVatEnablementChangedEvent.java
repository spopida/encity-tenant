package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.Getter;
import lombok.Setter;
import uk.co.encity.tenancy.commands.PatchTenancyCommand;
import uk.co.encity.tenancy.entity.Tenancy;

public class HmrcVatEnablementChangedEvent extends TenancyEvent {

    private boolean hmrcVatEnabled;

    public HmrcVatEnablementChangedEvent() {};

    public HmrcVatEnablementChangedEvent(
            PatchTenancyCommand cmd,
            Tenancy current,
            boolean hmrcVatEnabled) {
        super(
                TenancyEventType.HMRC_VAT_ENABLEMENT_CHANGED,
                current.getTenancyId(),
                current.getVersion() + 1,
                cmd.getCommandId());

        this.setHmrcVatEnabled(hmrcVatEnabled);
    }

    public boolean getHmrcVatEnabled() { return this.hmrcVatEnabled; }
    public boolean isHmrcVatEnabled() { return this.getHmrcVatEnabled(); }
    public void setHmrcVatEnabled(boolean value) { this.hmrcVatEnabled = value; }

    @Override
    public Tenancy applyToTenancy(Tenancy target) {
        target.setHmrcVatEnabled(this.isHmrcVatEnabled());
        return target;
    }

    @Override
    public void addSerializerToModule(SimpleModule module) {
        module.addSerializer(this.getClass(), new HmrcVatEnablementChangedEventSerializer());
    }

    @Override
    public String getRoutingKey() {
        return "encity.tenancy.hmrc_vat_enablement_changed";
    }
}
