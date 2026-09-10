package com.contactmanager.domain.model;

import com.contactmanager.domain.exception.InvalidContactException;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;


public record Contact(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String company,
        String jobTitle,
        String address,
        Instant createdAt,
        Instant updatedAt) {

    public static final int NAME_MAX_LENGTH = 100;
    public static final int EMAIL_MAX_LENGTH = 254;
    public static final int PHONE_MAX_LENGTH = 40;
    public static final int COMPANY_MAX_LENGTH = 150;
    public static final int JOB_TITLE_MAX_LENGTH = 150;
    public static final int ADDRESS_MAX_LENGTH = 255;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@.]+(\\.[^\\s@.]+)+$");

    public Contact {
        firstName = required("firstName", firstName, NAME_MAX_LENGTH);
        lastName = required("lastName", lastName, NAME_MAX_LENGTH);
        email = email(email);
        phoneNumber = optional("phoneNumber", phoneNumber, PHONE_MAX_LENGTH);
        company = optional("company", company, COMPANY_MAX_LENGTH);
        jobTitle = optional("jobTitle", jobTitle, JOB_TITLE_MAX_LENGTH);
        address = optional("address", address, ADDRESS_MAX_LENGTH);
    }

    public String fullName() {
        return firstName + " " + lastName;
    }

    public Contact withDetailsOf(Contact other, Instant now) {
        return new Contact(
                id,
                other.firstName,
                other.lastName,
                other.email,
                other.phoneNumber,
                other.company,
                other.jobTitle,
                other.address,
                createdAt,
                now);
    }

    private static String email(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidContactException("email", "email is required");
        }
        String normalised = value.trim().toLowerCase();
        if (normalised.length() > EMAIL_MAX_LENGTH) {
            throw new InvalidContactException("email", "email must be at most " + EMAIL_MAX_LENGTH + " characters");
        }
        if (!EMAIL_PATTERN.matcher(normalised).matches()) {
            throw new InvalidContactException("email", "email is not a valid address");
        }
        return normalised;
    }

    private static String required(String field, String value, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new InvalidContactException(field, field + " is required");
        }
        return checkLength(field, value.trim(), maxLength);
    }

    private static String optional(String field, String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return checkLength(field, value.trim(), maxLength);
    }

    private static String checkLength(String field, String value, int maxLength) {
        if (value.length() > maxLength) {
            throw new InvalidContactException(field, field + " must be at most " + maxLength + " characters");
        }
        return value;
    }
}
