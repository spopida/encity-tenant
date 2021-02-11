package uk.co.encity.tenancy.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.bson.Document;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import uk.co.encity.tenancy.commands.PatchTenancyCommand;
import uk.co.encity.tenancy.commands.PatchTenancyCommandDeserializer;

import java.io.IOException;

public class TenancyEventFactory {

    /**
     * The {@link Logger} for this class
     */
    private static final Logger logger = Loggers.getLogger(TenancyEventFactory.class);

    public static TenancyEvent getTenancyEvent(String tenancyId, TenancyEventType type, String jsonObj)
        throws InstantiationException
    {
        TenancyEvent evt = null;

        // Pass a de-serializer for a TenancyEvent - this will call an abstract (sub-class) method for
        // deserializing the sub-fields
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();

        // Pass in the type?
        module.addDeserializer(TenancyEvent.class, new TenancyEventDeserializer(tenancyId, jsonObj));
        mapper.registerModule(module);

        try {
            evt = mapper.readValue(jsonObj, TenancyEvent.class);
            logger.debug("Tenancy event de-serialised successfully");
        } catch (IOException e) {
            String msg = "Error de-serialising tenancy event for tenancyId: " + tenancyId + "; " + e.getMessage();
            logger.error(msg);
            throw new InstantiationException(msg);
        }
        return evt;
    }
}
