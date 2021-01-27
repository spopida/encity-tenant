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
    public void serialize(TenancyCreatedEvent value, JsonGenerator jgen, SerializerProvider provider)
      throws IOException, JsonProcessingException {

        jgen.writeStartObject();
        jgen.writeStringField("tariff", value.getTariff());
        jgen.writeFieldName("authorisedContact");
        jgen.writeObject(value.getAuthorisedContact());
        jgen.writeFieldName("confirmUUID");
        jgen.writeObject(value.getConfirmUUID());
        jgen.writeFieldName("expiryTime");
        jgen.writeObject(value.getExpiryTime());
        jgen.writeEndObject();
    }
}
