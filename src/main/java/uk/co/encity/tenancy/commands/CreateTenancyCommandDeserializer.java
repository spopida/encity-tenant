package uk.co.encity.tenancy.commands;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import uk.co.encity.tenancy.components.TenancyContact;

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
        TenancyContact authContact = this.deserializeContact(node.get("authorisedContact"));
        TenancyContact adminUsrContact = this.deserializeContact(node.get("adminUser"));

        TenancyContact billingContact = null;

        if (node.has("billingContact")) {
            billingContact = this.deserializeContact(node.get("billingContact"));
        } else {
            billingContact = new TenancyContact("","","");
        }

        return new CreateTenancyCommand(userId, tariff, authContact, adminUsrContact, billingContact);
    }

    private TenancyContact deserializeContact(JsonNode node) {
        String first = node.get("firstName").asText();
        String last = node.get("lastName").asText();
        String email = node.get("emailAddress").asText();

        return new TenancyContact(first, last, email);
    }

}
