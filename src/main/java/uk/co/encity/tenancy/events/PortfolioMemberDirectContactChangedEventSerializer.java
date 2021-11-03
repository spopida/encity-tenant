package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class PortfolioMemberDirectContactChangedEventSerializer extends StdSerializer<PortfolioMemberDirectContactChangedEvent> {
    public PortfolioMemberDirectContactChangedEventSerializer() { this(null); }

    public PortfolioMemberDirectContactChangedEventSerializer(Class<PortfolioMemberDirectContactChangedEvent> t) {
        super(t);
    }

    @Override
    public void serialize(
            PortfolioMemberDirectContactChangedEvent portfolioMemberDirectContactChangedEvent,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        jsonGenerator.writeStartObject();
        portfolioMemberDirectContactChangedEvent.writeCommonJson(jsonGenerator);
        jsonGenerator.writeStringField("companyId", portfolioMemberDirectContactChangedEvent.getCompanyId());
        jsonGenerator.writeStringField("directContact", portfolioMemberDirectContactChangedEvent.getDirectContact());
        jsonGenerator.writeEndObject();
    }
}