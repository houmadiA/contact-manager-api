package com.contactmanager.domain.model;

import java.util.Objects;

public record AuthenticationResult(AccessToken accessToken, RefreshToken refreshToken, User user) {

    public AuthenticationResult {
        Objects.requireNonNull(accessToken, "accessToken must not be null");
        Objects.requireNonNull(refreshToken, "refreshToken must not be null");
        Objects.requireNonNull(user, "user must not be null");
    }
}
