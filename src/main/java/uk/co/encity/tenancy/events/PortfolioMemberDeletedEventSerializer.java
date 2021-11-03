package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class PortfolioMemberDeletedEventSerializer extends StdSerializer<PortfolioMemberDeletedEvent> {
    public PortfolioMemberDeletedEventSerializer() { this(null); }

    public PortfolioMemberDeletedEventSerializer(Class<PortfolioMemberDeletedEvent> t) {
        super(t);
    }

    @Override
    public void serialize(
            PortfolioMemberDeletedEvent portfolioMemberDeletedEvent,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        jsonGenerator.writeStartObject();
        portfolioMemberDeletedEvent.writeCommonJson(jsonGenerator);
        jsonGenerator.writeStringField("companyId", portfolioMemberDeletedEvent.getCompanyId());
        jsonGenerator.writeEndObject();
    }
}
