package uk.co.encity.tenancy.events;

import org.json.JSONObject;

public class TenancyEventFactory {
    public static TenancyEvent getTenancyEvent(TenancyEventType type, String jsonObj) {
        TenancyEvent evt = null;

        switch (type) {
            case TENANCY_CREATED:
                evt = new TenancyCreatedEvent(jsonObj);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        };

        return evt;
    }
}
