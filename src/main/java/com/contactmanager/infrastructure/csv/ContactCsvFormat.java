package com.contactmanager.infrastructure.csv;

import java.util.List;

final class ContactCsvFormat {

    static final String FIRST_NAME = "firstName";
    static final String LAST_NAME = "lastName";
    static final String EMAIL = "email";
    static final String PHONE_NUMBER = "phoneNumber";
    static final String COMPANY = "company";
    static final String JOB_TITLE = "jobTitle";
    static final String ADDRESS = "address";

    static final String[] HEADERS = {
        FIRST_NAME, LAST_NAME, EMAIL, PHONE_NUMBER, COMPANY, JOB_TITLE, ADDRESS
    };

    static final List<String> REQUIRED_HEADERS = List.of(FIRST_NAME, LAST_NAME, EMAIL);

    static final String BYTE_ORDER_MARK = String.valueOf((char) 0xFEFF);

    private ContactCsvFormat() {}
}
