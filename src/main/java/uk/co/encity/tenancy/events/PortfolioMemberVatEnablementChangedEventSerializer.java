package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class PortfolioMemberVatEnablementChangedEventSerializer extends StdSerializer<PortfolioMemberVatEnablementChangedEvent> {
    public PortfolioMemberVatEnablementChangedEventSerializer() { this(null); }

    public PortfolioMemberVatEnablementChangedEventSerializer(Class<PortfolioMemberVatEnablementChangedEvent> t) {
        super(t);
    }

    @Override
    public void serialize(
            PortfolioMemberVatEnablementChangedEvent portfolioMemberVatEnablementChangedEvent,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        jsonGenerator.writeStartObject();
        portfolioMemberVatEnablementChangedEvent.writeCommonJson(jsonGenerator);
        jsonGenerator.writeStringField("companyId", portfolioMemberVatEnablementChangedEvent.getCompanyId());
        jsonGenerator.writeBooleanField("vatEnabled", portfolioMemberVatEnablementChangedEvent.getVatEnabled());
        jsonGenerator.writeEndObject();
    }

}
