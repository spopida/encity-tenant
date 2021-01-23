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

    public String getFirstName() { return this.firstName; }
    public String getLastName() { return this.lastName; }
    public String getEmailAddress() { return this.emailAddress; }
}
