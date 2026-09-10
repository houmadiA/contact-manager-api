package com.contactmanager.domain.model;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record User(UUID id, String username, String passwordHash, String displayName, Set<Role> roles) {

    public User {
        Objects.requireNonNull(id, "id must not be null");
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("passwordHash must not be blank");
        }
        username = username.trim();
        displayName = displayName == null || displayName.isBlank() ? username : displayName.trim();
        roles = Set.copyOf(Objects.requireNonNull(roles, "roles must not be null"));
        if (roles.isEmpty()) {
            throw new IllegalArgumentException("a user must have at least one role");
        }
    }
}
