package com.contactmanager.infrastructure.security;

import com.contactmanager.domain.port.out.RefreshTokenGenerator;
import java.security.SecureRandom;
import java.util.Base64;

public class SecureRandomRefreshTokenGenerator implements RefreshTokenGenerator {

    private static final int TOKEN_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    @Override
    public String newToken() {
        byte[] token = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(token);
        return encoder.encodeToString(token);
    }
}
