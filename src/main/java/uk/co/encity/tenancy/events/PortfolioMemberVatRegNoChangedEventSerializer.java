package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class PortfolioMemberVatRegNoChangedEventSerializer extends StdSerializer<PortfolioMemberVatRegNoChangedEvent> {
    public PortfolioMemberVatRegNoChangedEventSerializer() { this(null); }

    public PortfolioMemberVatRegNoChangedEventSerializer(Class<PortfolioMemberVatRegNoChangedEvent> t) {
        super(t);
    }

    @Override
    public void serialize(
            PortfolioMemberVatRegNoChangedEvent portfolioMemberVatRegNoChangedEvent,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        jsonGenerator.writeStartObject();
        portfolioMemberVatRegNoChangedEvent.writeCommonJson(jsonGenerator);
        jsonGenerator.writeStringField("companyId", portfolioMemberVatRegNoChangedEvent.getCompanyId());
        jsonGenerator.writeStringField("vatRegNo", portfolioMemberVatRegNoChangedEvent.getVatRegNo());
        jsonGenerator.writeEndObject();
    }

}