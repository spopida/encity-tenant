package uk.co.encity.tenancy.entity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import uk.co.encity.tenancy.components.TenancyContact;
import uk.co.encity.tenancy.events.TenancyCreatedEvent;

import java.io.IOException;
import java.time.Instant;

public class TenancySerializer extends StdSerializer<Tenancy> {
    public TenancySerializer() {
        this(null);
    }

    public TenancySerializer(Class<Tenancy> t) {
        super(t);
    }

    @Override
    public void serialize(Tenancy value, JsonGenerator jGen, SerializerProvider provider)
            throws IOException, JsonProcessingException {

        jGen.writeStartObject();

        // Convert ObjectId to hex string to avoid publishing a JSON object (simpler)
        jGen.writeStringField("tenancyId", value.getTenancyId().toHexString());
        jGen.writeStringField("tenancyName", value.getName());
        jGen.writeNumberField("version", value.getVersion());
        jGen.writeObjectField("lastUpdate", value.getLastUpdate());
        jGen.writeStringField("tariff", value.getTariff());
        jGen.writeObjectField("authorisedContact", value.getAuthorisedContact());
        jGen.writeObjectField("billingContact", value.getBillingContact());
        jGen.writeObjectField("tenantStatus", value.getTenantStatus());
        jGen.writeObjectField("providerStatus", value.getProviderStatus());

        jGen.writeEndObject();

        return;
    }
}
