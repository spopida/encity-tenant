package uk.co.encity.tenancy.entity;

import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.annotation.Link;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import reactor.util.Logger;
import reactor.util.Loggers;
import uk.co.encity.tenancy.components.TenancyContact;
import uk.co.encity.tenancy.events.TenancyCreatedEvent;
import uk.co.encity.tenancy.events.TenancyEvent;
import uk.co.encity.tenancy.snapshot.TenancySnapshot;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

public class Tenancy {

    /**
     * The {@link Logger} for this class
     */
    private final Logger logger = Loggers.getLogger(getClass());

    private ObjectId tenancyId;
    private String tenancyName;

    private int version;

    private Instant lastUpdate;

    private String tariff;
    private TenancyContact authorisedContact;
    private TenancyContact billingContact;
    private TenancyTenantStatus tenantStatus;
    private TenancyProviderStatus providerStatus;
    private UUID confirmUUID;
    private Instant confirmExpiryTime;

    public Tenancy() {}

    public static Tenancy fromSnapshot(TenancySnapshot snap) {
        Tenancy t = new Tenancy();
        t.tenancyId = snap.getTenancyId();
        t.tariff = snap.getTariff();
        t.tenancyName = snap.getName();
        t.lastUpdate = snap.getLastUpdate();
        t.version = snap.getToVersion();
        t.authorisedContact = snap.getAuthorisedContact();
        t.billingContact = snap.getBillingContact();
        t.tenantStatus = snap.getTenantStatus();
        t.providerStatus = snap.getProviderStatus();
        t.confirmUUID = snap.getConfirmUUID();
        t.confirmExpiryTime = snap.getConfirmExpiryTime();

        return t;
    }

    public ObjectId getTenancyId() { return this.tenancyId; }
    public String getHexTenancyId() { return this.tenancyId.toHexString(); }
    public String getName() { return this.tenancyName; }
    public int getVersion() { return this.version; }
    public Instant getLastUpdate() { return this.lastUpdate; }
    public String getTariff() { return this.tariff; }
    public TenancyContact getAuthorisedContact() { return this.authorisedContact; }
    public TenancyContact getBillingContact() { return this.billingContact; }
    public TenancyTenantStatus getTenantStatus() { return this.tenantStatus; }
    public TenancyProviderStatus getProviderStatus() { return this.providerStatus; }
    public UUID getConfirmUUID() { return this.confirmUUID; }
    public String getConfirmUUIDString() { return this.confirmUUID.toString(); }
    public Instant getConfirmExpiryTime() { return this.confirmExpiryTime; }

    public void setAuthorisedContact(TenancyContact authContact) {
        this.authorisedContact = authContact;
    }

    public TenancyView getView() throws TenancyException {

        // Make sure to use getters, not instance vars (there could be logic in them)

        TenancyView view = new TenancyView();

        try {
            byte[] decodedHex = Hex.decodeHex(this.getHexTenancyId());
            view.id = Base64.encodeBase64URLSafeString(decodedHex);
        } catch (DecoderException e) {
            logger.error("Unable to convert tenancy id to base 64; hex id is: " + this.getHexTenancyId());
            throw new TenancyException(e.getMessage());
        }

        view.name = this.getName();
        view.version = this.getVersion();
        view.lastUpdate = this.getLastUpdate().toString(); // TODO: check format, zone, etc
        view.tariff = this.getTariff();
        view.authorisedContact = this.getAuthorisedContact();
        view.billingContact = this.getBillingContact();
        view.tenantStatus = this.getTenantStatus().toString();

        return view;
    }
}
