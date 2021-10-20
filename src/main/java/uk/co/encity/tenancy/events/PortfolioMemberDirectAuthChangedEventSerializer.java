package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class PortfolioMemberDirectAuthChangedEventSerializer extends StdSerializer<PortfolioMemberDirectAuthChangedEvent> {
    public PortfolioMemberDirectAuthChangedEventSerializer() { this(null); }

    public PortfolioMemberDirectAuthChangedEventSerializer(Class<PortfolioMemberDirectAuthChangedEvent> t) {
        super(t);
    }

    @Override
    public void serialize(
            PortfolioMemberDirectAuthChangedEvent portfolioMemberDirectAuthChangedEvent,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        jsonGenerator.writeStartObject();
        portfolioMemberDirectAuthChangedEvent.writeCommonJson(jsonGenerator);
        jsonGenerator.writeStringField("companyId", portfolioMemberDirectAuthChangedEvent.getCompanyId());
        jsonGenerator.writeBooleanField("directAuth", portfolioMemberDirectAuthChangedEvent.getDirectAuth());
        jsonGenerator.writeEndObject();
    }

}
