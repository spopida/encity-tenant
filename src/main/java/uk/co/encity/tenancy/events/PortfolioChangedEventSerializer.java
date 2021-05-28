package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class PortfolioChangedEventSerializer extends StdSerializer<PortfolioChangedEvent> {

    public PortfolioChangedEventSerializer() {
        this(null);
    }

    public PortfolioChangedEventSerializer(Class<PortfolioChangedEvent> t) {
        super(t);
    }

    @Override
    public void serialize(PortfolioChangedEvent event, JsonGenerator jGen, SerializerProvider serializerProvider)
            throws IOException {
        jGen.writeStartObject();
        event.writeJson(jGen);
        jGen.writeFieldName("newPortfolio");
        jGen.writeObject(event.getNewPortfolio());
        jGen.writeEndObject();
    }
}
