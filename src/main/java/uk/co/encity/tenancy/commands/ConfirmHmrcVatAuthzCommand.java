package uk.co.encity.tenancy.commands;

import lombok.Getter;
import uk.co.encity.tenancy.entity.LastHmrcVatAuthzRequest;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.TenancyAvailabilityStatus;
import uk.co.encity.tenancy.entity.VatSettings;
import uk.co.encity.tenancy.events.HmrcVatAuthzConfirmedEvent;
import uk.co.encity.tenancy.events.TenancyEvent;

import java.io.IOException;

@Getter
public class ConfirmHmrcVatAuthzCommand extends PatchTenancyCommand {

    private String authzCode;
    private String companyNumber;
    private String requestUuid;
    private String redirectUri;


    public ConfirmHmrcVatAuthzCommand(
            String tenancyId,
            String companyNumber,
            String authzCode,
            String uuid,
            String redirectUri
    ) {
        super(TenancyTenantCommandType.CONFIRM_HMRC_VAT_AUTHZ, tenancyId);
        this.authzCode = authzCode;
        this.companyNumber = companyNumber;
        this.requestUuid = uuid;
        this.redirectUri = redirectUri;
    }

    @Override
    public void checkPreConditions(Tenancy t) throws PreConditionException {
        if (t.getAvailabilityStatus() != TenancyAvailabilityStatus.OPERATIONAL) {
            throw new PreConditionException( String.format( "Cannot alter tenancy %s since it is not operational", t.getHexTenancyId()));
        }

        // The uuid should be the latest request for the company number
        VatSettings vs = t.getPortfolioMemberForHmrcVatAuthz(this.requestUuid);
        LastHmrcVatAuthzRequest lr = null;
        if ( vs != null) {
            lr = vs.getLastHmrcVatAuthzRequest();
        }

        // TODO: Improve debug logging below

        if (lr == null) {
            throw new PreConditionException( String.format("Confirmation of HMRC VAT authorisation failed unexpectedly because the authorisation request could not be retrieved"));
        }

        if (! lr.getRequestUUID().toString().equals( this.requestUuid)) {
            throw new PreConditionException(String.format("Confirmation of HMRC VAT authorisation failed because another authorisation request has been issued"));
        }

        if (lr.getStatus() != LastHmrcVatAuthzRequest.LastHmrcVatAuthzRequestStatus.PENDING) {
            throw new PreConditionException(String.format("Confirmation of HMRC VAT authorisation failed because the request status is %s", lr.getStatus().toString()));
        }
    }

    @Override
    public TenancyEvent createTenancyEvent(Tenancy t) {
        // We need to get an email address so that the event that is published allows
        // someone to be emailed.  Here, we get the email address of the contact defined
        // for the portfolio member.  This *probably* the person who authorised the request,
        // but we can't be sure because:
        //
        //  a) they might have forwarded the request to someone else, and
        //  b) their email address might have been replaced/deleted since they received the request

        String probableAuthoriserEmailAddress = "";
        VatSettings vs = t.getPortfolioMemberForHmrcVatAuthz(this.requestUuid);
        if (vs != null) {
            probableAuthoriserEmailAddress = vs.getDirectContactEmail();
        }

        return new HmrcVatAuthzConfirmedEvent(
                this,
                t,
                this.authzCode,
                this.companyNumber,
                probableAuthoriserEmailAddress,
                this.redirectUri
        );
    }
}
