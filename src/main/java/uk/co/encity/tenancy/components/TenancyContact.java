package uk.co.encity.tenancy.components;

public class TenancyContact {
    private String firstName;
    private String lastName;
    private String emailAddress;

    public TenancyContact(String firstName, String lastName, String emailAddress) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
    }

    public TenancyContact() {
        ;
    }

    public String getFirstName() { return this.firstName; }
    public String getLastName() { return this.lastName; }
    public String getEmailAddress() { return this.emailAddress; }

    public void setFirstName(String n) { this.firstName = n; }
    public void setLastName(String n) { this.lastName = n; }
    public void setEmailAddress(String e) { this.emailAddress = e; }
}
