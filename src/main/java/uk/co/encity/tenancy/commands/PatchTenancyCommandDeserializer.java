package uk.co.encity.tenancy.commands;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class PatchTenancyCommandDeserializer extends StdDeserializer<PatchTenancyCommand> {

    private String hexTenancyId;
    private String companyId;

    public PatchTenancyCommandDeserializer(String hexTenancyId) {
        this((Class<?>) null, hexTenancyId);
    }

    public PatchTenancyCommandDeserializer(String hexTenancyId, String companyId) { this(null, hexTenancyId, companyId); }

    public PatchTenancyCommandDeserializer(Class<?> valueClass, String hexTenancyId) {
        super(valueClass);
        this.hexTenancyId = hexTenancyId;
        this.companyId = null;
    }

    public PatchTenancyCommandDeserializer(Class<?> valueClass, String hexTenancyId, String companyId) {
        super(valueClass);
        this.hexTenancyId = hexTenancyId;
        this.companyId = companyId;
    }

    public boolean isForPortfolioMember() { return companyId != null;  }
    public boolean isForAggregateRoot() { return companyId == null; }

    @Override
    public PatchTenancyCommand deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        String transition = node.get("action").asText();

        TenancyCommand.TenancyTenantCommandType cmdType = TenancyCommand.ACTION_MAP.get(transition);
        if (cmdType == null) throw new UnsupportedOperationException("Command " + transition + " is not supported");

        PatchTenancyCommand result = null;

        if ( this.isForAggregateRoot()) {
            result = PatchTenancyCommand.getPatchTenancyCommand(
                    cmdType,
                    this.hexTenancyId,
                    node
            );
        } else {
            result = PatchTenancyCommand.getPatchTenancyCommand(
                    cmdType,
                    this.hexTenancyId,
                    this.companyId,
                    node
            );
        }
        return result;
    }
}
