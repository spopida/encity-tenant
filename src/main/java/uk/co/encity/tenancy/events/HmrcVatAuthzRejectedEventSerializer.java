package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class HmrcVatAuthzRejectedEventSerializer extends StdSerializer<HmrcVatAuthzRejectedEvent> {

    public HmrcVatAuthzRejectedEventSerializer() { this(null); }
    public HmrcVatAuthzRejectedEventSerializer(Class<HmrcVatAuthzRejectedEvent> type) { super(type); }

    @Override
    public void serialize(
            HmrcVatAuthzRejectedEvent event,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider) throws IOException, JsonProcessingException {

        jsonGenerator.writeStartObject();
        event.writeCommonJson(jsonGenerator);

        // We may have to serialize more details (e.g. the authorised contact of the tenancy), depending
        // on what side effects we want to implement.  For example we might wish to notify the authorised
        // contact that an authz event has been rejected...but let's cross that bridge when we come to it
        jsonGenerator.writeObjectField("requestUUID", event.getRequestUuid());

        jsonGenerator.writeEndObject();
    }
}
