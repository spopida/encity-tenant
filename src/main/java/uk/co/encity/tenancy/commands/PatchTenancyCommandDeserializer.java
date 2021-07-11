package uk.co.encity.tenancy.commands;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.net.UnknownServiceException;

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

        TenancyCommand.TenancyTenantCommandType cmdType = TenancyCommand.ACTION_MAP.get(transition);
        if (cmdType == null) throw new UnsupportedOperationException("Command " + transition + " is not supported");

        PatchTenancyCommand result = PatchTenancyCommand.getPatchTenancyCommand(
                cmdType,
                this.hexTenancyId,
                node
        );
        return result;
    }
}
