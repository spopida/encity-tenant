package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.bson.types.ObjectId;
import uk.co.encity.tenancy.commands.PatchTenancyCommand;
import uk.co.encity.tenancy.commands.TenancyCommand;

import java.io.IOException;
import java.time.Instant;

public class TenancyEventDeserializer extends StdDeserializer<TenancyEvent> {

    private String hexTenancyId;
    private String jsonObj;

    public TenancyEventDeserializer(String hexTenancyId, String jsonObj) {
        this(null, hexTenancyId, jsonObj);
    }

    public TenancyEventDeserializer(Class<?> valueClass, String hexTenancyId, String jsonObj) {
        super(valueClass);
        this.hexTenancyId = hexTenancyId;
        this.jsonObj = jsonObj;
    }

    @Override
    public TenancyEvent deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);

        String evtType = node.get("eventType").asText();

        TenancyEvent evt = null;
        try {
            evt = TenancyEvent.getTenancyEvent(TenancyEventType.valueOf(evtType), node);
        } catch (InstantiationException e) {
            ; // log an error
            throw new IOException(e);
        }
        return evt;
    }
}
