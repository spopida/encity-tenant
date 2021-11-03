package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class PortfolioMemberAddedEventSerializer extends StdSerializer<PortfolioMemberAddedEvent> {
    public PortfolioMemberAddedEventSerializer() { this(null); }

    public PortfolioMemberAddedEventSerializer(Class<PortfolioMemberAddedEvent> t) {
        super(t);
    }

    @Override
    public void serialize(
            PortfolioMemberAddedEvent portfolioMemberAddedEvent,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        jsonGenerator.writeStartObject();
        portfolioMemberAddedEvent.writeCommonJson(jsonGenerator);
        jsonGenerator.writeStringField("companyId", portfolioMemberAddedEvent.getCompanyId());
        jsonGenerator.writeBooleanField("vatEnabled", portfolioMemberAddedEvent.isVatEnabled());
        jsonGenerator.writeStringField("vatRegNo", portfolioMemberAddedEvent.getVatRegNo());
        jsonGenerator.writeBooleanField("directAuthorisation", portfolioMemberAddedEvent.isDirectAuthorisation());
        jsonGenerator.writeStringField("directContactEmail", portfolioMemberAddedEvent.getDirectContactEmail());
        jsonGenerator.writeEndObject();
    }
}
