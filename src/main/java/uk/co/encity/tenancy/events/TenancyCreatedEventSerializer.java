package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class TenancyCreatedEventSerializer extends StdSerializer<TenancyCreatedEvent> {

    public TenancyCreatedEventSerializer() {
        this(null);
    }

    public TenancyCreatedEventSerializer(Class<TenancyCreatedEvent> t) {
        super(t);
    }

    @Override
    public void serialize(TenancyCreatedEvent value, JsonGenerator jGen, SerializerProvider provider)
      throws IOException, JsonProcessingException {

        jGen.writeStartObject();

        jGen.writeFieldName("tenancyId");
        // Convert ObjectId to hex string to avoid publishing a JSON object (simpler)
        jGen.writeString(value.getTenancyId().toHexString());
        jGen.writeStringField("tariff", value.getTariff());
        jGen.writeFieldName("authorisedContact");
        jGen.writeObject(value.getAuthorisedContact());
        jGen.writeFieldName("confirmUUID");
        jGen.writeObject(value.getConfirmUUID());
        jGen.writeFieldName("expiryTime");
        jGen.writeObject(value.getExpiryTime());
        jGen.writeEndObject();
    }
}
