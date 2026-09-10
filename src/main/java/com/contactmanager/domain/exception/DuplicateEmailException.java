package com.contactmanager.domain.exception;

public class DuplicateEmailException extends DomainException {

    private final String email;

    public DuplicateEmailException(String email) {
        super("A contact with email " + email + " already exists");
        this.email = email;
    }

    public String email() {
        return email;
    }
}
