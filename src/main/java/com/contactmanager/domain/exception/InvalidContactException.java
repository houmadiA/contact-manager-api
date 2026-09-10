package com.contactmanager.domain.exception;

public class InvalidContactException extends DomainException {

    private final String field;

    public InvalidContactException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String field() {
        return field;
    }
}
