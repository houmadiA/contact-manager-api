package com.contactmanager.infrastructure.security;

import com.contactmanager.domain.model.AccessToken;
import com.contactmanager.domain.model.Role;
import com.contactmanager.domain.model.User;
import com.contactmanager.domain.port.out.AccessTokenIssuer;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

public class JwtAccessTokenIssuer implements AccessTokenIssuer {

    private final JwtEncoder jwtEncoder;
    private final Clock clock;
    private final Duration tokenTtl;
    private final String issuer;

    public JwtAccessTokenIssuer(JwtEncoder jwtEncoder, Clock clock, Duration tokenTtl, String issuer) {
        this.jwtEncoder = jwtEncoder;
        this.clock = clock;
        this.tokenTtl = tokenTtl;
        this.issuer = issuer;
    }

    @Override
    public AccessToken issue(User user) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(tokenTtl);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(user.id().toString())
                .claim("username", user.username())
                .claim("name", user.displayName())
                .claim("roles", user.roles().stream().map(Role::name).sorted().toList())
                .build();

        String token = jwtEncoder
                .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();

        return new AccessToken(token, expiresAt);
    }
}
