package com.contactmanager.domain.model;

import java.util.Objects;

public record Credentials(String username, String password) {

    public Credentials {
        Objects.requireNonNull(username, "username must not be null");
        Objects.requireNonNull(password, "password must not be null");
    }
}
