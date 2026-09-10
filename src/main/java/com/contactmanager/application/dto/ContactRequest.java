package com.contactmanager.application.dto;

public record ContactRequest(
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String company,
        String jobTitle,
        String address) {
}
