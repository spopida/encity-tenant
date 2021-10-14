package uk.co.encity.tenancy.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VatSettings {
    boolean vatEnabled;
    String vatRegNbr;
    boolean directAuthorisation;
    String directContactEmail;

    public VatSettings() {
        this.vatEnabled = false;
        this.vatRegNbr = "";
        this.directAuthorisation = true;
        this.directContactEmail = "";
    }
}
