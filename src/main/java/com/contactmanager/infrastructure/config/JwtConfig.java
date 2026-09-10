package com.contactmanager.infrastructure.config;

import com.contactmanager.domain.port.out.AccessTokenIssuer;
import com.contactmanager.infrastructure.security.JwtAccessTokenIssuer;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtConfig {

    private static final int MINIMUM_SECRET_BYTES = 32;

    private final SecretKeySpec signingKey;

    JwtConfig(ContactManagerProperties properties) {
        String secret = properties.jwt().secret();
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MINIMUM_SECRET_BYTES) {
            throw new IllegalStateException(
                    "contact-manager.jwt.secret must be set and at least " + MINIMUM_SECRET_BYTES + " bytes long");
        }
        this.signingKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(signingKey));
    }

    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(signingKey).build();
    }

    @Bean
    AccessTokenIssuer accessTokenIssuer(
            JwtEncoder jwtEncoder, Clock clock, ContactManagerProperties properties) {
        return new JwtAccessTokenIssuer(
                jwtEncoder, clock, properties.jwt().accessTokenTtl(), properties.jwt().issuer());
    }
}
