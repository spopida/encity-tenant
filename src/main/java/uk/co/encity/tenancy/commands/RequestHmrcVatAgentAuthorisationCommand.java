package uk.co.encity.tenancy.commands;

import lombok.Getter;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.TenancyAvailabilityStatus;
import uk.co.encity.tenancy.events.HmrcVatAgentAuthorisationRequestedEvent;
import uk.co.encity.tenancy.events.TenancyEvent;

/**
 * The command has no extra detail associated with it (other than what is in the base class), but
 * will be recorded against a Tenancy and should trigger authorisation workflow
 */
@Deprecated
@Getter
public class RequestHmrcVatAgentAuthorisationCommand extends PatchTenancyCommand {

    public RequestHmrcVatAgentAuthorisationCommand(String hexTenancyId) {
        super(TenancyTenantCommandType.REQUEST_HMRC_AGENT_VAT_AUTHORISATION, hexTenancyId);
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
        return new HmrcVatAgentAuthorisationRequestedEvent(this, t);
    }
}
