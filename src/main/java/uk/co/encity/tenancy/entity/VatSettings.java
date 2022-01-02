package uk.co.encity.tenancy.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VatSettings {
    boolean vatEnabled;
    String vatRegNo;
    @Deprecated boolean directAuthorisation;
    String directContactEmail;
    LastHmrcVatAuthzRequest lastAuthzRequest;

    public VatSettings() {
        this.vatEnabled = false;
        this.vatRegNo = "";
        this.directAuthorisation = true;
        this.directContactEmail = "";
        this.lastAuthzRequest = null;
    }
}
