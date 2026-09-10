package com.contactmanager.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record RefreshToken(UUID userId, String token, Instant expiresAt) {

    public RefreshToken {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(token, "token must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
    }

    public boolean isExpiredAt(Instant instant) {
        return !instant.isBefore(expiresAt);
    }
}
