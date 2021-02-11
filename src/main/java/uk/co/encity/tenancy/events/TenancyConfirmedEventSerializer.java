package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class TenancyConfirmedEventSerializer extends StdSerializer<TenancyConfirmedEvent> {

    public TenancyConfirmedEventSerializer() {
        this(null);
    }

    public TenancyConfirmedEventSerializer(Class<TenancyConfirmedEvent> t) {
        super(t);
    }

    @Override
    public void serialize(TenancyConfirmedEvent value, JsonGenerator jGen, SerializerProvider provider)
            throws IOException, JsonProcessingException {

        jGen.writeStartObject();
        value.writeJson(jGen);
        jGen.writeEndObject();
    }
}
