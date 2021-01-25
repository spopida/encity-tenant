package uk.co.encity.tenancy.events;

import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.Transient;
import reactor.util.Logger;
import reactor.util.Loggers;

import uk.co.encity.tenancy.commands.CreateTenancyCommand;
import uk.co.encity.tenancy.components.TenancyContact;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class TenancyCreatedEvent extends TenancyEvent {

    private final Logger logger = Loggers.getLogger(getClass());

    private String tariff;
    private ObjectId commandId;
    private TenancyContact authorisedContact;
    private TenancyContact billingContact;
    private UUID confirmUUID;
    private Instant creationTime;

    public TenancyCreatedEvent(CreateTenancyCommand cmd) {
        super(TenancyEventType.TENANCY_CREATED, 1, cmd.getCommandId(), cmd.getAuthorisedContact().getEmailAddress(), cmd.getUserId());

        this.tariff = cmd.getTariff();
        this.authorisedContact = cmd.getAuthorisedContact();
        this.billingContact = cmd.getBillingContact();
        this.confirmUUID = UUID.randomUUID();
        this.creationTime = Instant.now();

        logger.debug("Tenancy created event constructed for domain: " + this.getDomain());
    }

    // TODO: don't hard code 1 hour...
    @BsonIgnore
    public Instant getExpiryTime() { return this.creationTime.plus(1, ChronoUnit.HOURS); }
    public Instant getCreationTime() { return this.creationTime; }
    public UUID getConfirmUUID() { return this.confirmUUID; }
    public String getTariff() { return this.tariff; }
    public TenancyContact getAuthorisedContact() { return this.authorisedContact; }
    public TenancyContact getBillingContact() { return this.billingContact; }
}
