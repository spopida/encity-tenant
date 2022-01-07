package uk.co.encity.tenancy.commands;

import lombok.Getter;
import uk.co.encity.tenancy.entity.LastHmrcVatAuthzRequest;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.TenancyAvailabilityStatus;
import uk.co.encity.tenancy.entity.VatSettings;
import uk.co.encity.tenancy.events.HmrcVatAuthzRejectedEvent;
import uk.co.encity.tenancy.events.TenancyEvent;

/**
 * Make a request directly to a company to authorise the software to access
 * the HMRC VAT records for the company, on behalf of the tenant
 */
@Getter
public class RejectHmrcVatAuthzCommand extends PatchTenancyCommand {

    private String requestUuid;

    public RejectHmrcVatAuthzCommand(
            String hexTenancyId,
            String requestUuid) {
        super(TenancyTenantCommandType.REJECT_HMRC_VAT_AUTHZ, hexTenancyId);
        this.requestUuid = requestUuid;
    }

    @Override
    public void checkPreConditions(Tenancy t) throws PreConditionException {
        if (t.getAvailabilityStatus() != TenancyAvailabilityStatus.OPERATIONAL) {
            throw new PreConditionException( String.format( "Cannot alter tenancy %s since it is not operational", t.getHexTenancyId()));
        }

        // Check that the request that is being rejected exists and is PENDING
        VatSettings vs = t.getPortfolioMemberForHmrcVatAuthz(this.requestUuid);
        if (vs != null) {
            LastHmrcVatAuthzRequest last = vs.getLastHmrcVatAuthzRequest();
            if (last.getStatus() == LastHmrcVatAuthzRequest.LastHmrcVatAuthzRequestStatus.PENDING) {
                return; // Happy path
            } else {
                throw new PreConditionException(
                        String.format(
                                "Cannot reject HMRC VAT Authorisation because the status of the request is not PENDING. (Request UUID: %s, Current Status: %s)",
                                this.requestUuid,
                                last.getStatus().toString()));
            }
        } else {
            throw new PreConditionException(
                    String.format(
                            "Cannot reject authorization request because there is no portfolio member with a request that matches the uuid (%s)",
                            this.requestUuid));
        }
    }

    @Override
    public TenancyEvent createTenancyEvent(Tenancy t) {
        return new HmrcVatAuthzRejectedEvent(
                this,
                t,
                this.requestUuid);
    }
}
