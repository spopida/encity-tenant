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
        this.status = this.getPendingOrExpired(creationEvent.getExpiry());
    }

    private LastHmrcVatAuthzRequestStatus getPendingOrExpired(Instant expiryTime) {
        return expiryTime.isBefore(Instant.now())
                ? LastHmrcVatAuthzRequestStatus.EXPIRED
                : LastHmrcVatAuthzRequestStatus.PENDING;
    }

    public LastHmrcVatAuthzRequestStatus getStatus() {
        if (this.status != LastHmrcVatAuthzRequestStatus.PENDING) {
            return this.status;
        } else {
            // TAKE NOTE: THIS GETTER HAS A SIDE-EFFECT OF UPDATING THE STATUS!
            return (this.status = getPendingOrExpired(this.creationEvent.getExpiry()));

            // That might be a bad thing in the context of supposedly idempotent operations,
            // But it seems to be the right thing at the time of writing...
            //
            // ...it seems like simply observing this object could mutate it, because
            // of the passage of time.  Quantum or what?
        }
    }

    public UUID getRequestUUID() {
        return this.creationEvent.getRequestUUID();
    }
}
