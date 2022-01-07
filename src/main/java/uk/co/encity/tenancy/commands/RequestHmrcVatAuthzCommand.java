package uk.co.encity.tenancy.commands;

import lombok.Getter;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.TenancyAvailabilityStatus;
import uk.co.encity.tenancy.events.HmrcVatAuthzRequestedEvent;
import uk.co.encity.tenancy.events.TenancyEvent;

/**
 * Make a request directly to a company to authorise the software to access
 * the HMRC VAT records for the company, on behalf of the tenant
 */
@Getter
public class RequestHmrcVatAuthzCommand extends PatchTenancyCommand {

    private String companyNumber;
    private String companyName;
    private String contactEmail;
    private String domain;

    public RequestHmrcVatAuthzCommand(
            String hexTenancyId,
            String companyNumber,
            String companyName,
            String contactEmail,
            String domain) {
        super(TenancyTenantCommandType.REQUEST_HMRC_VAT_AUTHZ, hexTenancyId);
        this.companyNumber = companyNumber;
        this.companyName = companyName;
        this.contactEmail = contactEmail;
        this.domain = domain;
    }

    @Override
    public void checkPreConditions(Tenancy t) throws PreConditionException {
        if (t.getAvailabilityStatus() != TenancyAvailabilityStatus.OPERATIONAL) {
            throw new PreConditionException( String.format( "Cannot alter tenancy %s since it is not operational", t.getHexTenancyId()));
        }

        // There's a bit more we could do here, like...
        // - check that the company number is in the portfolio
        // - make sure the contact email matches
        // - make sure the domain matches the tenancy
        // - make sure VAT is enabled

        return;
    }

    @Override
    public TenancyEvent createTenancyEvent(Tenancy t) {
        return new HmrcVatAuthzRequestedEvent(
                this,
                t,
                this.companyNumber,
                this.companyName,
                this.contactEmail,
                this.domain);
    }
}
