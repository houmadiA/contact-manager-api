package com.contactmanager.application.controller;

import com.contactmanager.application.dto.LoginRequest;
import com.contactmanager.application.dto.UserResponse;
import com.contactmanager.application.mapper.ContactWebMapper;
import com.contactmanager.application.security.AuthCookieFactory;
import com.contactmanager.domain.exception.InvalidCredentialsException;
import com.contactmanager.domain.model.AuthenticationResult;
import com.contactmanager.domain.model.Credentials;
import com.contactmanager.domain.port.in.AuthenticationService;
import com.contactmanager.domain.port.out.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Open, refresh and close a cookie-backed session")
@RequiredArgsConstructor
class AuthController {

    private final AuthenticationService authentication;
    private final UserRepository users;
    private final ContactWebMapper mapper;
    private final AuthCookieFactory cookies;

    @PostMapping("/login")
    @Operation(summary = "Exchange credentials for a session carried by http-only cookies")
    ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        return session(authentication.login(new Credentials(request.username(), request.password())));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate the refresh token cookie and issue a fresh access token cookie")
    ResponseEntity<UserResponse> refresh(
            @CookieValue(name = AuthCookieFactory.REFRESH_TOKEN, required = false) String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new InvalidCredentialsException();
        }
        return session(authentication.refresh(refreshToken));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke the refresh token and clear both cookies")
    ResponseEntity<Void> logout(
            @CookieValue(name = AuthCookieFactory.REFRESH_TOKEN, required = false) String refreshToken) {
        if (StringUtils.hasText(refreshToken)) {
            authentication.logout(refreshToken);
        }
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookies.clearedAccessToken().toString())
                .header(HttpHeaders.SET_COOKIE, cookies.clearedRefreshToken().toString())
                .build();
    }

    @GetMapping("/me")
    @Operation(summary = "Describe the currently signed-in user")
    UserResponse currentUser(@AuthenticationPrincipal Jwt jwt) {
        return users.findById(UUID.fromString(jwt.getSubject()))
                .map(mapper::toResponse)
                .orElseThrow(InvalidCredentialsException::new);
    }

    private ResponseEntity<UserResponse> session(AuthenticationResult result) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookies.accessToken(result.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, cookies.refreshToken(result.refreshToken()).toString())
                .body(mapper.toResponse(result.user()));
    }
}
