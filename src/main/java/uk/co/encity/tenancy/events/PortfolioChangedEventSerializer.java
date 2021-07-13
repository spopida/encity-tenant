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
        event.writeCommonJson(jGen);
        jGen.writeObjectField("newPortfolio", event.getNewPortfolio());
        jGen.writeObjectField("newPortfolioDetails", event.getNewPortfolioDetails());
        jGen.writeEndObject();
    }
}
