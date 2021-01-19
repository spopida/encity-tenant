package uk.co.encity.tenant;

import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONObject;

import java.time.Instant;

public class CreateTenancyCommand extends TenancyCommand {

    public static class CTCContact {
        String firstName;
        String lastName;
        String emailAddress;
        protected CTCContact(String first, String last, String email) {
            this.firstName = first;
            this.lastName = last;
            this.emailAddress = email;
        }

        public String getFirstName() { return this.firstName; }
        public String getLastName() { return this.lastName; }
        public String getEmailAddress() { return this.emailAddress; }
    }

    private String tariff;
    private CTCContact authorisedContact;
    private CTCContact adminUser;
    private CTCContact billingContact;

    public CreateTenancyCommand(String userId, String tariff, CTCContact authorisedContact, CTCContact adminUser, CTCContact billingContact) {
        super(userId);
        this.tariff = tariff;
        this.authorisedContact = authorisedContact;
        this.adminUser = adminUser;
        this.billingContact = billingContact;
    }

    public String getTariff() {
        return this.tariff;
    }

    public CTCContact getAuthorisedContact() { return this.authorisedContact; }
    public CTCContact getAdminUser() { return this.adminUser; };
    public CTCContact getBillingContact() { return this.billingContact; }
}
