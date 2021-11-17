package uk.co.encity.tenancy.commands;

import lombok.Getter;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.TenancyAvailabilityStatus;
import uk.co.encity.tenancy.events.HmrcVatDirectAuthorisationRequestedEvent;
import uk.co.encity.tenancy.events.TenancyEvent;

/**
 * Make a request directly to a company to authorise the software to access
 * the HMRC VAT records for the company, on behalf of the tenant
 */
@Getter
public class RequestHmrcVatDirectAuthorisationCommand extends PatchTenancyCommand {

    private String companyNumber;

    public RequestHmrcVatDirectAuthorisationCommand(String hexTenancyId, String companyNumber) {
        super(TenancyTenantCommandType.REQUEST_HMRC_VAT_DIRECT_AUTHORISATION, hexTenancyId);
        this.companyNumber = companyNumber;
    }

    @Override
    public void checkPreConditions(Tenancy t) throws PreConditionException {
        if (t.getAvailabilityStatus() != TenancyAvailabilityStatus.OPERATIONAL) {
            throw new PreConditionException( String.format( "Cannot alter tenancy %s since it is not operational", t.getHexTenancyId()));
        }
        return;
    }

    @Override
    public TenancyEvent createTenancyEvent(Tenancy t) {
        return new HmrcVatDirectAuthorisationRequestedEvent(this, t, this.companyNumber);
    }
}
