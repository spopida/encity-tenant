package uk.co.encity.tenancy.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VatSettings {
    boolean vatEnabled;
    String vatRegNbr;

    public VatSettings() {
        this.vatEnabled = false;
        this.vatRegNbr = "";
    }
}
