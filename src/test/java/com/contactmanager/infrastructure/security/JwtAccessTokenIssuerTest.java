package com.contactmanager.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.contactmanager.domain.model.AccessToken;
import com.contactmanager.domain.model.Role;
import com.contactmanager.domain.model.User;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

class JwtAccessTokenIssuerTest {

    private static final Instant NOW = Instant.parse("2026-03-01T09:00:00Z");
    private static final Duration TTL = Duration.ofMinutes(15);
    private static final byte[] SECRET = "a-test-secret-that-is-long-enough-for-hs256".getBytes();

    private final SecretKeySpec key = new SecretKeySpec(SECRET, "HmacSHA256");
    private final JwtAccessTokenIssuer issuer = new JwtAccessTokenIssuer(
            new NimbusJwtEncoder(new ImmutableSecret<>(key)),
            Clock.fixed(NOW, ZoneOffset.UTC),
            TTL,
            "contact-manager");

    private final JwtDecoder decoder = decoderReadingTheClockAs(NOW);

    private final User user = new User(
            UUID.randomUUID(), "demo", "irrelevant-hash", "Demo User", Set.of(Role.USER, Role.ADMIN));

    @Test
    void identifiesTheUser() {
        Jwt jwt = decoder.decode(issuer.issue(user).value());

        assertThat(jwt.getSubject()).isEqualTo(user.id().toString());
        assertThat(jwt.getClaimAsString("username")).isEqualTo("demo");
        assertThat(jwt.getClaimAsString("name")).isEqualTo("Demo User");
    }

    @Test
    void expiresAfterConfiguredLifetime() {
        AccessToken accessToken = issuer.issue(user);

        assertThat(accessToken.expiresAt()).isEqualTo(NOW.plus(TTL));
        assertThat(decoder.decode(accessToken.value()).getExpiresAt()).isEqualTo(NOW.plus(TTL));
    }

    private NimbusJwtDecoder decoderReadingTheClockAs(Instant instant) {
        JwtTimestampValidator timestamps = new JwtTimestampValidator();
        timestamps.setClock(Clock.fixed(instant, ZoneOffset.UTC));

        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(key).build();
        jwtDecoder.setJwtValidator(timestamps);
        return jwtDecoder;
    }
}
