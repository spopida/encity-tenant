package uk.co.encity.tenancy.entity;

import uk.co.encity.tenancy.components.TenancyContact;

import java.util.List;
import java.util.Map;

/**
 * A simple view of a {@link Tenancy} that only shows what should be visible
 * external to the service.  This should only be constructed by a {@link Tenancy} instance
 */
public class TenancyView {
    public String id;
    public String name;
    public int version;
    public String lastUpdate;
    public String tariff;
    public TenancyContact authorisedContact;
    public TenancyContact billingContact;
    public TenancyContact originalAdminUser;
    public String tenantStatus;
    public String domain;
    public List<String> defaultPortfolio;
    public boolean isHmrcVatEnabled;
    public Map<String, VatSettings> portfolioDetails;
    public boolean isHmrcVatAuthorisationRequestPending;

    //TODO: lastHmrcAuthorisation will be the creation datetime of the _AUTHORISED event.
    // When that event is applied, this attribute will be updated.  The nextExpectedAuthorisation
    // will be derived purely through logic, but this will be done here, not in a front-end

    protected TenancyView() {
        ;
    }
}
