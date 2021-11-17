package uk.co.encity.tenancy.entity;

public class DirectAuthorisationRequestException extends Exception {

    public DirectAuthorisationRequestException(String errorMsg) {
        super(errorMsg);
    }
    public DirectAuthorisationRequestException(String errorMsg, Throwable t) {
        super(errorMsg, t);
    }
}
