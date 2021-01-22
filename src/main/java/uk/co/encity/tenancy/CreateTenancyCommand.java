package uk.co.encity.tenancy;

public class CreateTenancyCommand extends TenancyCommand {

    private String tariff;
    private TenancyContact authorisedContact;
    private TenancyContact adminUser;
    private TenancyContact billingContact;

    public CreateTenancyCommand(String userId, String tariff, TenancyContact authorisedContact, TenancyContact adminUser, TenancyContact billingContact) {
        super(userId);
        this.tariff = tariff;
        this.authorisedContact = authorisedContact;
        this.adminUser = adminUser;
        this.billingContact = billingContact;
    }

    public String getTariff() {
        return this.tariff;
    }

    public TenancyContact getAuthorisedContact() { return this.authorisedContact; }
    public TenancyContact getAdminUser() { return this.adminUser; };
    public TenancyContact getBillingContact() { return this.billingContact; }
}
