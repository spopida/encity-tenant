package uk.co.encity.tenancy.events;

import org.bson.types.ObjectId;
import reactor.util.Logger;
import reactor.util.Loggers;

import uk.co.encity.tenancy.commands.CreateTenancyCommand;
import uk.co.encity.tenancy.components.TenancyContact;

// TODO: Create an abstract parent class with common attributes / methods
public class TenancyCreatedEvent extends TenancyEvent {

    private final Logger logger = Loggers.getLogger(getClass());

    private String tariff;
    private ObjectId commandId;
    private TenancyContact authorisedContact;
    private TenancyContact billingContact;
    private String confirmUUID;

    public TenancyCreatedEvent(CreateTenancyCommand cmd) {
        super(TenancyEventType.TENANCY_CREATED, 1, cmd.getCommandId(), cmd.getAuthorisedContact().getEmailAddress(), cmd.getUserId());

        this.tariff = cmd.getTariff();
        this.authorisedContact = cmd.getAuthorisedContact();
        this.billingContact = cmd.getBillingContact();

        logger.debug("Tenancy created event constructed for domain: " + this.getDomain());
    }

    public String getTariff() { return this.tariff; }
    public TenancyContact getAuthorisedContact() { return this.authorisedContact; }
    public TenancyContact getBillingContact() { return this.billingContact; }
}
