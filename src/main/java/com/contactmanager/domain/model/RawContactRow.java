package com.contactmanager.domain.model;


public record RawContactRow(
        int lineNumber,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String company,
        String jobTitle,
        String address) {
}
