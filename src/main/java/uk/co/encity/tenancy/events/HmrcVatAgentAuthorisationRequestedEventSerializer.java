package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class HmrcVatAgentAuthorisationRequestedEventSerializer extends StdSerializer<HmrcVatAgentAuthorisationRequestedEvent> {

    public HmrcVatAgentAuthorisationRequestedEventSerializer() { this( null ); }
    public HmrcVatAgentAuthorisationRequestedEventSerializer(Class<HmrcVatAgentAuthorisationRequestedEvent> t) { super(t); }

    @Override
    public void serialize(
            HmrcVatAgentAuthorisationRequestedEvent event,
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
