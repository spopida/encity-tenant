package uk.co.encity.tenancy.entity;

import lombok.Getter;
import lombok.Setter;
import uk.co.encity.tenancy.events.HmrcVatAuthzRequestedEvent;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class LastHmrcVatAuthzRequest {

    public enum LastHmrcVatAuthzRequestStatus {
        PENDING,
        EXPIRED,
        AUTHORISED,
        REJECTED
    }

    private final HmrcVatAuthzRequestedEvent creationEvent;
    private LastHmrcVatAuthzRequestStatus status;

    public LastHmrcVatAuthzRequest(HmrcVatAuthzRequestedEvent creationEvent) {
        this.creationEvent = creationEvent;
        this.status =
                creationEvent.getExpiry().isBefore(Instant.now())
                        ? LastHmrcVatAuthzRequestStatus.EXPIRED
                        : LastHmrcVatAuthzRequestStatus.PENDING;
    }

    public UUID getRequestUUID() {
        return this.creationEvent.getRequestUUID();
    }
}
