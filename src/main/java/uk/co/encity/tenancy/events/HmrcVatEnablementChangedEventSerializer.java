package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class HmrcVatEnablementChangedEventSerializer extends StdSerializer<HmrcVatEnablementChangedEvent> {

    public HmrcVatEnablementChangedEventSerializer() { this(null); }

    public HmrcVatEnablementChangedEventSerializer(Class<HmrcVatEnablementChangedEvent> t) { super(t); }

    @Override
    public void serialize(
            HmrcVatEnablementChangedEvent hmrcVatEnablementChangedEvent,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider) throws IOException, JsonProcessingException {

        jsonGenerator.writeStartObject();
        hmrcVatEnablementChangedEvent.writeCommonJson(jsonGenerator);
        jsonGenerator.writeBooleanField("isHmrcVatEnabled", hmrcVatEnablementChangedEvent.isHmrcVatEnabled());
        jsonGenerator.writeEndObject();
    }
}
