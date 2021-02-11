package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;
import reactor.util.Logger;
import reactor.util.Loggers;

import uk.co.encity.tenancy.commands.CreateTenancyCommand;
import uk.co.encity.tenancy.components.TenancyContact;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.TenancyProviderStatus;
import uk.co.encity.tenancy.entity.TenancyTenantStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class TenancyCreatedEvent extends TenancyEvent {

    private final Logger logger = Loggers.getLogger(getClass());

    private String tariff;
    private TenancyContact authorisedContact;
    private TenancyContact billingContact;
    private TenancyContact adminUser;
    private UUID confirmUUID;
    private Instant creationTime;
    private String domain;
    private TenancyTenantStatus tenantStatus;
    private TenancyProviderStatus providerStatus;
    private int expiryHours;

    public TenancyCreatedEvent() {}

    public TenancyCreatedEvent(CreateTenancyCommand cmd, int expiryHours) {
        super(
            TenancyEventType.TENANCY_CREATED,
            new ObjectId(),
            1,
            cmd.getCommandId());

        this.tariff = cmd.getTariff();
        this.authorisedContact = cmd.getAuthorisedContact();
        this.billingContact = cmd.getBillingContact();
        this.adminUser = cmd.getAdminUser();
        this.confirmUUID = UUID.randomUUID();
        this.creationTime = Instant.now();
        String parts[] = cmd.getAuthorisedContact().getEmailAddress().split("@");
        this.domain = parts[1];
        this.tenantStatus = TenancyTenantStatus.UNCONFIRMED;
        this.providerStatus = TenancyProviderStatus.ACTIVE;
        this.expiryHours = expiryHours;


        logger.debug("Tenancy created event constructed for domain: " + this.getDomain());
    }

    @BsonIgnore
    public Instant getExpiryTime() { return this.creationTime.plus(this.expiryHours, ChronoUnit.HOURS); }
    public Instant getCreationTime() { return this.creationTime; }
    public UUID getConfirmUUID() { return this.confirmUUID; }
    public String getTariff() { return this.tariff; }
    public TenancyContact getAuthorisedContact() { return this.authorisedContact; }
    public TenancyContact getBillingContact() { return this.billingContact; }
    public TenancyContact getAdminUser() { return this.adminUser; }
    public String getDomain() { return this.domain; }
    public TenancyTenantStatus getTenantStatus() { return this.tenantStatus; }
    public TenancyProviderStatus getProviderStatus() { return this.providerStatus; }

    @Override
    public Tenancy applyToTenancy(Tenancy target) {
        // Do nothing - there should be no difference in the case
        // of a creation event!
        return target;
    }

    @Override
    public void addSerializerToModule(SimpleModule module) {
        module.addSerializer(this.getClass(), new TenancyCreatedEventSerializer());
    }
}
