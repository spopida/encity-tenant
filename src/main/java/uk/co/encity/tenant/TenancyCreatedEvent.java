package uk.co.encity.tenant;

import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Instant;

// TODO: Create an abstract parent class with common attributes / methods
public class TenancyCreatedEvent extends TenancyEvent {

    public class TenancyContact {
        private String firstName;
        private String lastName;
        private String emailAddress;

        public TenancyContact(CreateTenancyCommand.CTCContact ctcContact) {
            this.firstName = ctcContact.firstName;
            this.lastName = ctcContact.lastName;
            this.emailAddress = ctcContact.emailAddress;
        }

        public String getFirstName() { return this.firstName; }
        public String getLastName() { return this.lastName; }
        public String getEmailAddress() { return this.emailAddress; }
    }

    private final Logger logger = Loggers.getLogger(getClass());

    private String tariff;
    private TenancyContact authorisedContact;
    private TenancyContact billingContact;

    public TenancyCreatedEvent(CreateTenancyCommand cmd) {
        super(Tenancy.TenancyEventType.TENANCY_CREATED, cmd.getAuthorisedContact().getEmailAddress(), cmd.getUserId());

        this.tariff = cmd.getTariff();
        this.authorisedContact = new TenancyContact(cmd.getAuthorisedContact());
        this.billingContact = new TenancyContact(cmd.getBillingContact());

        logger.debug("Tenancy created event constructed for domain: " + this.getDomain());
    }

    public String getTariff() { return this.tariff; }
    public TenancyContact getAuthorisedContact() { return this.authorisedContact; }
    public TenancyContact getBillingContact() { return this.billingContact; }
}
