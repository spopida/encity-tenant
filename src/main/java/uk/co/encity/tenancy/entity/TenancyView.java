package uk.co.encity.tenancy.entity;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.bson.types.ObjectId;
import uk.co.encity.tenancy.components.TenancyContact;

import java.time.Instant;
import java.util.UUID;

/**
 * A simple view of a {@link Tenancy} that only shows what should be visible
 * external to the service.  This should only be constructed by a {@link Tenancy} instance
 */
public class TenancyView {
    public String id;
    public String name;
    public int version;
    public String lastUpdate;
    public String tariff;
    public TenancyContact authorisedContact;
    public TenancyContact billingContact;
    public String tenantStatus;

    protected TenancyView() {
        ;
    }
}