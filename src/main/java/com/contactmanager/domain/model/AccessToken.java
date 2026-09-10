package com.contactmanager.domain.model;

import java.time.Instant;
import java.util.Objects;

public record AccessToken(String value, Instant expiresAt) {

    public AccessToken {
        Objects.requireNonNull(value, "token value must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
    }
}
