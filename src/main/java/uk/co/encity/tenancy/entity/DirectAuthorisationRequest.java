package uk.co.encity.tenancy.entity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Deprecated
public class DirectAuthorisationRequest {

    public enum DirectAuthorisationRequestStatus {
        PENDING,
        EXPIRED,
        AUTHORISED,
        REJECTED,
        SUPERSEDED
    }

    public enum DirectAuthorisationRequestAction {
        UNACTIONED,
        AUTHORISED,
        REJECTED,
        SUPERSEDED
    }

    private final UUID requestId;
    private final Instant expiryTime;
    private final String addressee;
    private final String companyNumber;
    private DirectAuthorisationRequestAction authStatus;

    public DirectAuthorisationRequest(String addressee, String companyNumber, Instant expiry) {
        this.requestId  = UUID.randomUUID();
        this.expiryTime = expiry;
        this.authStatus = DirectAuthorisationRequestAction.UNACTIONED;
        this.addressee = addressee;
        this.companyNumber = companyNumber;
    }

    public UUID getRequestId() {
        return this.requestId;
    }

    public String getAddressee() {
        return this.addressee;
    }

    public String getCompanyNumber() {
        return this.companyNumber;
    }

    public void authorise() throws DirectAuthorisationRequestException {
        // Check unactioned and not expired
        if (this.authStatus != DirectAuthorisationRequestAction.UNACTIONED)
            throw new DirectAuthorisationRequestException("Request has already been actioned, so cannot be authorised");

        if (this.hasExpired()) {
            throw new DirectAuthorisationRequestException("Request has expired, so cannot be authorised");
        }

        this.authStatus = DirectAuthorisationRequestAction.AUTHORISED;
    }

    public void reject() throws DirectAuthorisationRequestException {
        if (this.authStatus != DirectAuthorisationRequestAction.UNACTIONED)
            throw new DirectAuthorisationRequestException("Request has already been actioned, so cannot be rejected");

        if (this.hasExpired()) {
            throw new DirectAuthorisationRequestException("Request has expired, so cannot be rejected");
        }

        this.authStatus = DirectAuthorisationRequestAction.REJECTED;
    }

    public void supersede() throws DirectAuthorisationRequestException {
        if (this.authStatus != DirectAuthorisationRequestAction.UNACTIONED)
            throw new DirectAuthorisationRequestException("Request has already been actioned, so cannot be superseded");

        if (this.hasExpired()) {
            throw new DirectAuthorisationRequestException("Request has expired, so cannot be superseded");
        }

        this.authStatus = DirectAuthorisationRequestAction.SUPERSEDED;
    }

    private boolean hasExpired() {
        return(Instant.now().isAfter(this.expiryTime));
    }

    public DirectAuthorisationRequestStatus getDirectAuthorisationRequestStatus() {

        DirectAuthorisationRequestStatus result = DirectAuthorisationRequestStatus.PENDING;

        switch(this.authStatus) {
            case UNACTIONED:
                result = (hasExpired() ? DirectAuthorisationRequestStatus.EXPIRED : DirectAuthorisationRequestStatus.PENDING);
                break;
            case AUTHORISED:
                result = DirectAuthorisationRequestStatus.AUTHORISED;
                break;
            case REJECTED:
                result = DirectAuthorisationRequestStatus.REJECTED;
                break;
            case SUPERSEDED:
                result = DirectAuthorisationRequestStatus.SUPERSEDED;
                break;
        }

        return result;
    }
}
