package com.contactmanager.infrastructure.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "contact-manager")
public record ContactManagerProperties(@DefaultValue Jwt jwt, @DefaultValue Cors cors) {

    public record Jwt(
            String secret,
            @DefaultValue("contact-manager") String issuer,
            @DefaultValue("15m") Duration accessTokenTtl,
            @DefaultValue("7d") Duration refreshTokenTtl) {}

    public record Cors(@DefaultValue("http://localhost:4200") List<String> allowedOrigins) {}
}
