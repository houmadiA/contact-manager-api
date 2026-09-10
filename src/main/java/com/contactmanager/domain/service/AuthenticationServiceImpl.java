package com.contactmanager.domain.service;

import com.contactmanager.domain.exception.InvalidCredentialsException;
import com.contactmanager.domain.model.AccessToken;
import com.contactmanager.domain.model.AuthenticationResult;
import com.contactmanager.domain.model.Credentials;
import com.contactmanager.domain.model.RefreshToken;
import com.contactmanager.domain.model.User;
import com.contactmanager.domain.port.in.AuthenticationService;
import com.contactmanager.domain.port.out.AccessTokenIssuer;
import com.contactmanager.domain.port.out.PasswordHasher;
import com.contactmanager.domain.port.out.RefreshTokenGenerator;
import com.contactmanager.domain.port.out.RefreshTokenStore;
import com.contactmanager.domain.port.out.UserRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final AccessTokenIssuer accessTokenIssuer;
    private final RefreshTokenStore refreshTokenStore;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final Clock clock;
    private final Duration refreshTokenTtl;

    public AuthenticationServiceImpl(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            AccessTokenIssuer accessTokenIssuer,
            RefreshTokenStore refreshTokenStore,
            RefreshTokenGenerator refreshTokenGenerator,
            Clock clock,
            Duration refreshTokenTtl) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.passwordHasher = Objects.requireNonNull(passwordHasher);
        this.accessTokenIssuer = Objects.requireNonNull(accessTokenIssuer);
        this.refreshTokenStore = Objects.requireNonNull(refreshTokenStore);
        this.refreshTokenGenerator = Objects.requireNonNull(refreshTokenGenerator);
        this.clock = Objects.requireNonNull(clock);
        this.refreshTokenTtl = Objects.requireNonNull(refreshTokenTtl);
    }

    @Override
    public AuthenticationResult login(Credentials command) {
        User user = userRepository
                .findByUsername(command.username())
                .filter(candidate -> passwordHasher.matches(command.password(), candidate.passwordHash()))
                .orElseThrow(InvalidCredentialsException::new);

        return issueTokensFor(user);
    }

    @Override
    public AuthenticationResult refresh(String refreshToken) {
        RefreshToken stored =
                refreshTokenStore.findByToken(refreshToken).orElseThrow(InvalidCredentialsException::new);

        if (stored.isExpiredAt(clock.instant())) {
            refreshTokenStore.delete(refreshToken);
            throw new InvalidCredentialsException();
        }

        User user = userRepository.findById(stored.userId()).orElseThrow(InvalidCredentialsException::new);

        refreshTokenStore.delete(refreshToken);
        return issueTokensFor(user);
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenStore.delete(refreshToken);
    }

    private AuthenticationResult issueTokensFor(User user) {
        AccessToken accessToken = accessTokenIssuer.issue(user);

        Instant expiresAt = clock.instant().plus(refreshTokenTtl);
        RefreshToken refreshToken = new RefreshToken(user.id(), refreshTokenGenerator.newToken(), expiresAt);
        refreshTokenStore.save(refreshToken);

        return new AuthenticationResult(accessToken, refreshToken, user);
    }
}
