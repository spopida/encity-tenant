package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.Getter;
import uk.co.encity.tenancy.commands.PatchTenancyCommand;
import uk.co.encity.tenancy.components.TenancyContact;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.service.BeanUtil;
import uk.co.encity.tenancy.service.ConfigProperties;

import java.time.Instant;
import java.util.UUID;

@Getter
public class HmrcVatAuthorisationRequestedEvent extends TenancyEvent {

    private UUID requestUUID;
    private String domain;
    private TenancyContact authContact;
    private Instant expiry;

    // Default constructor and setters are needed for de-serialization from the database
    public HmrcVatAuthorisationRequestedEvent() {};

    public void setRequestUUID(UUID uuid) { this.requestUUID = uuid; }
    public void setDomain(String d) { this.domain = d; }
    public void setAuthContact(TenancyContact contact) { this.authContact = contact; }
    public void setExpiry(Instant expiry) { this.expiry = expiry; }

    public HmrcVatAuthorisationRequestedEvent(PatchTenancyCommand cmd, Tenancy current) {
        super(
                TenancyEventType.HMRC_VAT_AUTHORISATION_REQUESTED,
                current.getTenancyId(),
                current.getVersion() + 1,
                cmd.getCommandId());
        ConfigProperties properties = BeanUtil.getBean(ConfigProperties.class); // This will initialize properties variable properly.
        this.requestUUID = UUID.randomUUID();
        this.domain = current.getDomain();
        this.authContact = current.getAuthorisedContact();
        int expiryHours = properties.getExpiryHours();
        this.expiry = this.getEventDateTime().plusSeconds(expiryHours * 60 * 60);
    }

    @Override
    public Tenancy applyToTenancy(Tenancy target) {
        target.setHmrcVatAuthorisationRequestPending(true);
        target.setHmrcVatAuthorisationRequestUUID(this.requestUUID);
        target.setHmrcVatAuthorisationRequestExpiry(this.getExpiry());
        return target;
    }

    @Override
    public void addSerializerToModule(SimpleModule module) {
        module.addSerializer(this.getClass(), new HmrcVatAuthorisationRequestedEventSerializer());
        return;
    }

    @Override
    public String getRoutingKey() {
        // The following key is not vat-specific...I expect it to be used
        // for any kind fo HMRC-related authorisation request
        return "encity.tenancy.hmrc_authorisation_requested";
    }
}
