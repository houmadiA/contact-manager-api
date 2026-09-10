package com.contactmanager.domain.exception;

public class CsvImportException extends DomainException {

    public CsvImportException(String message) {
        super(message);
    }

    public CsvImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
