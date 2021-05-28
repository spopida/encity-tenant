package uk.co.encity.tenancy.commands;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class PatchTenancyCommandDeserializer extends StdDeserializer<PatchTenancyCommand> {

    private String hexTenancyId;

    public PatchTenancyCommandDeserializer(String hexTenancyId) {
        this(null, hexTenancyId);
    }

    public PatchTenancyCommandDeserializer(Class<?> valueClass, String hexTenancyId) {
        super(valueClass);
        this.hexTenancyId = hexTenancyId;
    }

    @Override
    public PatchTenancyCommand deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        String transition = node.get("action").asText();

        PatchTenancyCommand result = PatchTenancyCommand.getPatchTenancyCommand(
                TenancyCommand.ACTION_MAP.get(transition),
                this.hexTenancyId,
                node
        );
        return result;
    }
}
