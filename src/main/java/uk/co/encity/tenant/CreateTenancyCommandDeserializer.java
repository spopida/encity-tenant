package uk.co.encity.tenant;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

/**
 * Deserializes a JSON create tenancy command into a CreateTenancyCommand object
 */
public class CreateTenancyCommandDeserializer extends StdDeserializer<CreateTenancyCommand> {

    String userId = null;

    public CreateTenancyCommandDeserializer() {
        this(null);
    }

    public CreateTenancyCommandDeserializer(Class<?> valueClass) {
        super(valueClass);
    }

    public CreateTenancyCommandDeserializer setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    @Override
    public CreateTenancyCommand deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {

        if (userId == null) throw new IOException("User Id has not been set on deserializer");

        JsonNode node = jp.getCodec().readTree(jp);
        String tariff = node.get("tariff").asText();
        CreateTenancyCommand.CTCContact authContact = this.deserializeContact(node.get("authorisedContact"));
        CreateTenancyCommand.CTCContact adminUsrContact = this.deserializeContact(node.get("adminUser"));

        CreateTenancyCommand.CTCContact billingContact = null;

        if (node.has("billingContact")) {
            billingContact = this.deserializeContact(node.get("billingContact"));
        } else {
            billingContact = new CreateTenancyCommand.CTCContact("","","");
        }

        return new CreateTenancyCommand(userId, tariff, authContact, adminUsrContact, billingContact);
    }

    private CreateTenancyCommand.CTCContact deserializeContact(JsonNode node) {
        String first = node.get("firstName").asText();
        String last = node.get("lastName").asText();
        String email = node.get("emailAddress").asText();

        return new CreateTenancyCommand.CTCContact(first, last, email);
    }

}
