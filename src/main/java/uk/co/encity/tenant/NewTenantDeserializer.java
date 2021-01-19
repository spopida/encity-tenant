package uk.co.encity.tenant;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

/**
 * Deserializes a JSON new tenant DTO into a Tenancy entity
 */
public class NewTenantDeserializer extends StdDeserializer<Tenancy> {
    public NewTenantDeserializer() {
        this(null);
    }

    public NewTenantDeserializer(Class<?> valueClass) {
        super(valueClass);
    }

    @Override
    public Tenancy deserialize(JsonParser jp, DeserializationContext ctxt) throws
        IOException,
        JsonProcessingException {

        JsonNode node = jp.getCodec().readTree(jp);
        String tariff = node.get("tariff").asText();
        Contact authContact = this.deserializeContact(node.get("authorisedContact"));
        Contact adminUsrContact = this.deserializeContact(node.get("adminUser"));

        Contact billingContact = null;

        if (node.has("billingContact")) {
            billingContact = this.deserializeContact(node.get("billingContact"));
        } else {
            billingContact = new Contact();
        }

        return new Tenancy(tariff, authContact, adminUsrContact, billingContact);
    }

    private Contact deserializeContact(JsonNode node) {
        String first = node.get("firstName").asText();
        String last = node.get("lastName").asText();
        String email = node.get("emailAddress").asText();

        return new Contact(first, last, email);
    }

}
