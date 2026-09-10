package com.contactmanager.application.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CookieBearerTokenResolver implements BearerTokenResolver {

    private final BearerTokenResolver headerResolver = new DefaultBearerTokenResolver();

    @Override
    public String resolve(HttpServletRequest request) {
        String fromHeader = headerResolver.resolve(request);
        return fromHeader != null ? fromHeader : fromCookie(request);
    }

    private static String fromCookie(HttpServletRequest request) {
        return Optional.ofNullable(request.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(cookie -> AuthCookieFactory.ACCESS_TOKEN.equals(cookie.getName()))
                        .map(Cookie::getValue)
                        .filter(StringUtils::hasText)
                        .findFirst())
                .orElse(null);
    }
}
