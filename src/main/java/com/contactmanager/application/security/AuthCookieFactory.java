package com.contactmanager.application.security;

import com.contactmanager.domain.model.AccessToken;
import com.contactmanager.domain.model.RefreshToken;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthCookieFactory {

    public static final String ACCESS_TOKEN = "cm_access_token";
    public static final String REFRESH_TOKEN = "cm_refresh_token";

    private static final String API_PATH = "/api";
    private static final String AUTH_PATH = "/api/auth";

    private final AuthCookieProperties properties;
    private final Clock clock;

    public ResponseCookie accessToken(AccessToken token) {
        return cookie(ACCESS_TOKEN, token.value(), API_PATH, timeUntil(token.expiresAt()));
    }

    public ResponseCookie refreshToken(RefreshToken token) {
        return cookie(REFRESH_TOKEN, token.token(), AUTH_PATH, timeUntil(token.expiresAt()));
    }

    public ResponseCookie clearedAccessToken() {
        return cookie(ACCESS_TOKEN, "", API_PATH, Duration.ZERO);
    }

    public ResponseCookie clearedRefreshToken() {
        return cookie(REFRESH_TOKEN, "", AUTH_PATH, Duration.ZERO);
    }

    private ResponseCookie cookie(String name, String value, String path, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(properties.secure())
                .sameSite(properties.sameSite())
                .path(path)
                .maxAge(maxAge)
                .build();
    }

    private Duration timeUntil(Instant expiresAt) {
        Duration remaining = Duration.between(clock.instant(), expiresAt);
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }
}
