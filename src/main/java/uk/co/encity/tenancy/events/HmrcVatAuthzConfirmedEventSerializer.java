package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class HmrcVatAuthzConfirmedEventSerializer extends StdSerializer<HmrcVatAuthzConfirmedEvent> {

    public HmrcVatAuthzConfirmedEventSerializer() { this( null); }
    public HmrcVatAuthzConfirmedEventSerializer(Class<HmrcVatAuthzConfirmedEvent> type) { super(type); }

    @Override
    public void serialize(
            HmrcVatAuthzConfirmedEvent event,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();
        event.writeCommonJson(jsonGenerator);

        jsonGenerator.writeStringField("authzCode", event.getAuthzCode());
        jsonGenerator.writeStringField("tenancyId", event.getTenancyId().toString());
        jsonGenerator.writeStringField("companyNumber", event.getCompanyNumber());
        jsonGenerator.writeStringField("originatorEmailAddress", event.getOriginatorEmailAddress());
        jsonGenerator.writeStringField("tenancyDomain", event.getTenancyDomain());
        jsonGenerator.writeStringField("redirectUri", event.getRedirectUri());

        jsonGenerator.writeEndObject();
    }
}
