package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import uk.co.encity.tenancy.commands.PatchTenancyCommand;
import uk.co.encity.tenancy.components.TenancyContact;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.service.BeanUtil;
import uk.co.encity.tenancy.service.ConfigProperties;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.UUID;

import static java.lang.Integer.parseInt;

@Getter
public class HmrcVatAuthorisationRequestedEvent extends TenancyEvent {

    private UUID requestUUID;
    private String domain;
    private TenancyContact authContact;
    private Instant expiry;

    public HmrcVatAuthorisationRequestedEvent() {};

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
