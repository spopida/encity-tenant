package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class TenancyRejectedEventSerializer extends StdSerializer<TenancyRejectedEvent> {

    public TenancyRejectedEventSerializer() {
        this(null);
    }

    public TenancyRejectedEventSerializer(Class<TenancyRejectedEvent> t) {
        super(t);
    }

    @Override
    public void serialize(TenancyRejectedEvent value, JsonGenerator jGen, SerializerProvider provider)
            throws IOException, JsonProcessingException {

        jGen.writeStartObject();
        value.writeCommonJson(jGen);
        jGen.writeEndObject();
    }
}
