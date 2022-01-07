package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.Getter;
import lombok.Setter;
import uk.co.encity.tenancy.commands.PatchTenancyCommand;
import uk.co.encity.tenancy.entity.LastHmrcVatAuthzRequest;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.VatSettings;
import uk.co.encity.tenancy.service.BeanUtil;
import uk.co.encity.tenancy.service.ConfigProperties;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Getter
@Setter
public class HmrcVatAuthzRequestedEvent extends TenancyEvent {

    private UUID requestUUID;
    private String companyNumber;
    private String companyName;
    private String domain;
    private String contactEmail;
    private Instant expiry;

    // Default constructor and setters are needed for de-serialization from the database
    public HmrcVatAuthzRequestedEvent() {};

    /*
    public void setRequestUUID(UUID uuid) { this.requestUUID = uuid; }
    public void setExpiry(Instant expiry) { this.expiry = expiry; }

     */

    public HmrcVatAuthzRequestedEvent(
            PatchTenancyCommand cmd,
            Tenancy current,
            String companyNumber,
            String companyName,
            String contactEmail,
            String domain) {
        super(
                TenancyEventType.HMRC_VAT_AUTHZ_REQUESTED,
                current.getTenancyId(),
                current.getVersion() + 1,
                cmd.getCommandId());
        ConfigProperties properties = BeanUtil.getBean(ConfigProperties.class); // This will initialize properties variable properly.
        this.requestUUID = UUID.randomUUID();
        int expiryHours = properties.getExpiryHours();
        this.expiry = this.getEventDateTime().plus(expiryHours, ChronoUnit.HOURS);
        this.companyNumber = companyNumber;
        this.companyName = companyName;
        this.contactEmail = contactEmail;
        this.domain = domain;
    }

    @Override
    public Tenancy applyToTenancy(Tenancy target) {
        VatSettings vs = target.getPortfolioDetails().get(this.companyNumber);
        if (vs != null) {
            vs.setLastHmrcVatAuthzRequest(new LastHmrcVatAuthzRequest(this));
        }
        return target;
    }

    @Override
    public void addSerializerToModule(SimpleModule module) {
        module.addSerializer(this.getClass(), new HmrcVatAuthzRequestedEventSerializer());
        return;
    }

    @Override
    public String getRoutingKey() {
        return "encity.tenancy.hmrc_vat_authz_requested";
    }
}
