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

@Getter
@Setter
public class HmrcVatAuthzRejectedEvent extends TenancyEvent {

    /*
     * The UUID of the event that is being rejected.  This is represented
     * as a String because there is no need for it to be in native form
     */
    private String requestUuid;

    // Default constructor and setters are needed for de-serialization from the database
    public HmrcVatAuthzRejectedEvent() {};

    public HmrcVatAuthzRejectedEvent(
            PatchTenancyCommand cmd,
            Tenancy current,
            String requestUuid)
    {
        super(
                TenancyEventType.HMRC_VAT_AUTHZ_REJECTED,
                current.getTenancyId(),
                current.getVersion() + 1,
                cmd.getCommandId());

        ConfigProperties properties = BeanUtil.getBean(ConfigProperties.class); // This will initialize properties variable properly.
        this.requestUuid = requestUuid;
    }

    @Override
    public Tenancy applyToTenancy(Tenancy target) {
        // Set the status of the LastAuthzRequest (for the portfolio member) to REJECTED
        VatSettings vs = target.getPortfolioMemberForHmrcVatAuthz(this.requestUuid);
        if (vs != null) {
            LastHmrcVatAuthzRequest lastAuthzRequest = vs.getLastHmrcVatAuthzRequest();
            if (lastAuthzRequest.getStatus() == LastHmrcVatAuthzRequest.LastHmrcVatAuthzRequestStatus.PENDING) {
                lastAuthzRequest.setStatus(LastHmrcVatAuthzRequest.LastHmrcVatAuthzRequestStatus.REJECTED);
            }
        }
        return target;
    }

    @Override
    public void addSerializerToModule(SimpleModule module) {
        module.addSerializer(this.getClass(), new HmrcVatAuthzRejectedEventSerializer());
        return;
    }

    @Override
    public String getRoutingKey() {
        return "encity.tenancy.hmrc_vat_authz_rejected";
    }
}
