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

/**
 * This event represents successful confirmation of delegated authority for GovBuddy to
 * access company's HMRC VAT records.  Success means that an authorisation code was received
 * from HMRC's consent portal.
 */
@Getter @Setter
public class HmrcVatAuthzConfirmedEvent extends TenancyEvent {

    // we'll need: code, tenancyId, companyNumber, email address
    private String authzCode;
    private String companyNumber;
    private String originatorEmailAddress;
    private String redirectUri;
    private String tenancyDomain;

    public HmrcVatAuthzConfirmedEvent() {}

    public HmrcVatAuthzConfirmedEvent(
            PatchTenancyCommand cmd,
            Tenancy current,
            String authzCode,
            String companyNumber,
            String originatorEmailAddress,
            String redirectUri
    ) {
        super(
                TenancyEventType.HMRC_VAT_AUTHZ_CONFIRMED,
                current.getTenancyId(),
                current.getVersion() + 1,
                cmd.getCommandId());

        ConfigProperties properties = BeanUtil.getBean(ConfigProperties.class); // This will initialize properties variable properly.

        this.authzCode = authzCode;
        this.companyNumber = companyNumber;
        this.originatorEmailAddress = originatorEmailAddress;
        this.redirectUri = redirectUri;
        this.tenancyDomain = current.getDomain();
    }

    @Override
    public Tenancy applyToTenancy(Tenancy target) {
        // Set the status of the LastAuthzRequest (for the portfolio member) to AUTHORISED
        VatSettings vs = target.getPortfolioDetails().get(this.companyNumber);
        if (vs != null) {
            LastHmrcVatAuthzRequest lastAuthzRequest = vs.getLastHmrcVatAuthzRequest();
            if (lastAuthzRequest.getStatus() == LastHmrcVatAuthzRequest.LastHmrcVatAuthzRequestStatus.PENDING) {
                lastAuthzRequest.setStatus(LastHmrcVatAuthzRequest.LastHmrcVatAuthzRequestStatus.AUTHORISED);
            }
        }
        return target;
    }

    @Override
    public void addSerializerToModule(SimpleModule module) {
        module.addSerializer(this.getClass(), new HmrcVatAuthzConfirmedEventSerializer());
        return;
    }

    @Override
    public String getRoutingKey() {
        return "encity.tenancy.hmrc_vat_authz_confirmed";
    }
}
