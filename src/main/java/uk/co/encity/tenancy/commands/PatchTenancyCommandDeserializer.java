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


    // I GOT UP TO HERE - NEED TO EXTRACT THE STRING ARRAY OF ENTITY IDs IN ORDER TO CONSTRUCT
    // A CHANGE PORTFOLIO COMMAND.  THEN THE GENERIC CONTROLLER PATCH METHOD SHOULD WORK OK
    //
    // THEN I NEED TO CHECK DATABASE PERSISTENCE AND RETRIEVAL (AND RE-SERIALIZATION) ARE WORKING OK
    //
    // THEN I CAN SWITCH BACK TO THE UI - RETRIEVE THE TENANCY (AND PORTFOLIO) IN THE DASHBOARD, AND INSTALL
    // A BUTTON HANDLER TO PATCH THE TENANCY WITH A NEW PORTFOLIO.  ALSO WILL NEED TO HANDLE THIS WHEN BINNING
    // AN ENTITY ON THE UI

    @Override
    public PatchTenancyCommand deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        String transition = node.get("action").asText();

        return PatchTenancyCommand.getPatchTenancyCommand(
            TenancyCommand.ACTION_MAP.get(transition),
            this.hexTenancyId,
            node
        );
    }
}
