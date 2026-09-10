package com.contactmanager.application.dto;

import java.time.Instant;

public record ContactResponse(
        String id,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phoneNumber,
        String company,
        String jobTitle,
        String address,
        Instant createdAt,
        Instant updatedAt) {
}
