package uk.co.encity.tenant;

public class Contact {
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String phoneNumber;

    public Contact() {
        this.firstName = "";
        this.lastName = "";
        this.emailAddress = "";
        this.phoneNumber = "";
    }

    public Contact(String firstName, String lastName, String emailAddr) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddr;
        this.phoneNumber = ""; // Not used at the moment
    }
}
