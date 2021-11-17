package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.Getter;
import uk.co.encity.tenancy.commands.PatchTenancyCommand;
import uk.co.encity.tenancy.components.TenancyContact;
import uk.co.encity.tenancy.entity.DirectAuthorisationRequest;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.service.BeanUtil;
import uk.co.encity.tenancy.service.ConfigProperties;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Getter
public class HmrcVatDirectAuthorisationRequestedEvent extends TenancyEvent {

    private UUID requestUUID;
    private String companyNumber;
    private Tenancy tenancy;
    private String directContactEmail;
    private Instant expiry;

    // Default constructor and setters are needed for de-serialization from the database
    public HmrcVatDirectAuthorisationRequestedEvent() {};

    public void setRequestUUID(UUID uuid) { this.requestUUID = uuid; }
    public void setExpiry(Instant expiry) { this.expiry = expiry; }

    public HmrcVatDirectAuthorisationRequestedEvent(PatchTenancyCommand cmd, Tenancy current, String companyNumber) {
        super(
                TenancyEventType.HMRC_VAT_DIRECT_AUTHORISATION_REQUESTED,
                current.getTenancyId(),
                current.getVersion() + 1,
                cmd.getCommandId());
        ConfigProperties properties = BeanUtil.getBean(ConfigProperties.class); // This will initialize properties variable properly.
        this.requestUUID = UUID.randomUUID();
        int expiryHours = properties.getExpiryHours();
        this.expiry = this.getEventDateTime().plus(expiryHours, ChronoUnit.HOURS);
        this.tenancy = current;
        this.companyNumber = companyNumber;

        // Attempt to find the company in the portfolio - if found then determine the contact to email

        // If the company isn't found, or the contact is blank, or vat is not enabled (?) then refuse to
        // construct; the command cannot be actioned as an event
    }

    @Override
    public Tenancy applyToTenancy(Tenancy target) {
        DirectAuthorisationRequest request = new DirectAuthorisationRequest(this.directContactEmail, this.companyNumber, this.expiry);

        // Perhaps we'll have to create a new collection here - direct authorisation requests
        // This will be subsidiary to a portfolio member (1:M)
        //
        // We'll need to track the UUID, the expiry, the intended recipient, the portfolio member,
        // and the authorisation status.  Auth status could be one of: UNAUTHORISED, AUTHORISED, REJECTED
        // expiry is a boolean
        //
        // Note that a request can only be superseded if it has not expired (like authorisation)
        // Overall status:
        // Auth Status  |  Expiry Status | Overall Status
        // -----------------------------------------------------
        // UNACTIONED   | UNEXPIRED      | PENDING
        // UNACTIONED   | EXPIRED        | EXPIRED
        // AUTHORISED   | UNEXPIRED      | AUTHORISED
        // AUTHORISED   | EXPIRED        | AUTHORISED
        // REJECTED     | UNEXPIRED      | REJECTED
        // REJECTED     | EXPIRED        | REJECTED
        // SUPERSEDED   | UNEXPIRED      | SUPERSEDED
        // SUPERSEDED   | EXPIRED        | SUPERSEDED

        // TODO: add the DirectAuthorisationRequest created above to the tenancy
        // - Be sure to set any existing requests for the same company to SUPERSEDED!
        // - THINK about the best way of doing this ... do I need to create any more events?
        // - Remember this is an insert-only database!
        return target;
    }

    @Override
    public void addSerializerToModule(SimpleModule module) {
        // TODO: ...
        // module.addSerializer(this.getClass(), new HmrcVatDirectAuthorisationRequestedEventSerializer());
        return;
    }

    @Override
    public String getRoutingKey() {
        return "encity.tenancy.hmrc_vat_direct_authorisation_requested";
    }
}
