package com.contactmanager.domain.exception;

import java.util.UUID;

public class ContactNotFoundException extends DomainException {

    private final UUID contactId;

    public ContactNotFoundException(UUID contactId) {
        super("Contact not found: " + contactId);
        this.contactId = contactId;
    }

    public UUID contactId() {
        return contactId;
    }
}
