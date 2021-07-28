package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class HmrcVatAuthorisationRequestedEventSerializer extends StdSerializer<HmrcVatAuthorisationRequestedEvent> {

    public HmrcVatAuthorisationRequestedEventSerializer() { this( null ); }
    public HmrcVatAuthorisationRequestedEventSerializer(Class<HmrcVatAuthorisationRequestedEvent> t) { super(t); }

    @Override
    public void serialize(
            HmrcVatAuthorisationRequestedEvent event,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("tenancyId", event.getTenancyId().toHexString());
        jsonGenerator.writeStringField("domain", event.getDomain());
        jsonGenerator.writeObjectField("authorisedContact", event.getAuthContact());
        jsonGenerator.writeObjectField("requestUUID", event.getRequestUUID());
        jsonGenerator.writeObjectField("expiryTime", event.getExpiry());

        jsonGenerator.writeEndObject();
    }
}
