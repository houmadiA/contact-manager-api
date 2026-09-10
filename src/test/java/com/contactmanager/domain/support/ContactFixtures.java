package com.contactmanager.domain.support;

import com.contactmanager.domain.model.Contact;
import com.contactmanager.domain.model.RawContactRow;
import com.contactmanager.domain.port.out.ContactCsvReader;
import java.io.InputStream;
import java.util.List;

public final class ContactFixtures {

    private ContactFixtures() {}

    public static Contact contact(String firstName, String lastName, String email) {
        return contact(firstName, lastName, email, null, null);
    }

    public static Contact contact(String firstName, String lastName, String email, String jobTitle) {
        return contact(firstName, lastName, email, jobTitle, null);
    }

    public static Contact contact(
            String firstName, String lastName, String email, String jobTitle, String company) {
        return new Contact(null, firstName, lastName, email, null, company, jobTitle, null, null, null);
    }

    public static RawContactRow row(int lineNumber, String firstName, String lastName, String email) {
        return new RawContactRow(lineNumber, firstName, lastName, email, null, null, null, null);
    }

    public static ContactCsvReader noCsvReader() {
        return csv -> List.of();
    }
}
