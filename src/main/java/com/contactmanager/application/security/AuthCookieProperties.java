package com.contactmanager.application.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "contact-manager.auth.cookie")
public record AuthCookieProperties(@DefaultValue("true") boolean secure, @DefaultValue("Strict") String sameSite) {
}
