package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class HmrcVatAuthzRequestedEventSerializer extends StdSerializer<HmrcVatAuthzRequestedEvent> {

    public HmrcVatAuthzRequestedEventSerializer() { this(null); }
    public HmrcVatAuthzRequestedEventSerializer(Class<HmrcVatAuthzRequestedEvent> type) { super(type); }

    @Override
    public void serialize(
            HmrcVatAuthzRequestedEvent event,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider) throws IOException, JsonProcessingException {

        jsonGenerator.writeStartObject();
        event.writeCommonJson(jsonGenerator);

        jsonGenerator.writeStringField("tenancyId", event.getTenancyIdHex());
        jsonGenerator.writeObjectField("requestUUID", event.getRequestUUID());
        jsonGenerator.writeStringField("companyNumber", event.getCompanyNumber());
        jsonGenerator.writeStringField("companyName", event.getCompanyName());
        jsonGenerator.writeStringField("domain", event.getDomain());
        jsonGenerator.writeStringField("contactEmail", event.getContactEmail());
        jsonGenerator.writeObjectField("expiry", event.getExpiry());

        jsonGenerator.writeEndObject();
    }
}
