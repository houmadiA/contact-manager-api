package com.contactmanager.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.contactmanager.domain.exception.InvalidCredentialsException;
import com.contactmanager.domain.model.AccessToken;
import com.contactmanager.domain.model.AuthenticationResult;
import com.contactmanager.domain.model.Credentials;
import com.contactmanager.domain.model.RefreshToken;
import com.contactmanager.domain.model.Role;
import com.contactmanager.domain.model.User;
import com.contactmanager.domain.port.in.AuthenticationService;
import com.contactmanager.domain.port.out.AccessTokenIssuer;
import com.contactmanager.domain.port.out.PasswordHasher;
import com.contactmanager.domain.port.out.RefreshTokenStore;
import com.contactmanager.domain.port.out.UserRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AuthenticationServiceTest {

    private static final Instant NOW = Instant.parse("2026-03-01T09:00:00Z");
    private static final Duration REFRESH_TTL = Duration.ofDays(7);

    private final User demoUser = new User(
            UUID.randomUUID(), "demo", "hashed:secret", "Demo User", Set.of(Role.USER));

    private InMemoryUserRepository users;
    private InMemoryRefreshTokenStore refreshTokens;
    private SequentialTokenGenerator tokenGenerator;
    private AuthenticationService service;

    @BeforeEach
    void setUp() {
        users = new InMemoryUserRepository(demoUser);
        refreshTokens = new InMemoryRefreshTokenStore();
        tokenGenerator = new SequentialTokenGenerator();
        service = new AuthenticationServiceImpl(
                users,
                new PrefixPasswordHasher(),
                new StubAccessTokenIssuer(),
                refreshTokens,
                tokenGenerator,
                Clock.fixed(NOW, ZoneOffset.UTC),
                REFRESH_TTL);
    }

    @Nested
    class Login {

        @Test
        void returnsTokensForValidCredentials() {
            AuthenticationResult result = service.login(new Credentials("demo", "secret"));

            assertThat(result.accessToken().value()).isEqualTo("access-for-demo");
            assertThat(result.accessToken().expiresAt()).isEqualTo(NOW.plus(Duration.ofMinutes(15)));
            assertThat(result.refreshToken().token()).isEqualTo("token-1");
            assertThat(result.refreshToken().expiresAt()).isEqualTo(NOW.plus(REFRESH_TTL));
            assertThat(result.user()).isEqualTo(demoUser);
            assertThat(refreshTokens.findByToken("token-1")).isPresent();
        }

        @Test
        void rejectsWrongPassword() {
            assertThatThrownBy(() -> service.login(new Credentials("demo", "wrong")))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Invalid username or password");
        }

    }

    @Nested
    class Refresh {

        @Test
        void rotatesRefreshToken() {
            String firstRefreshToken =
                    service.login(new Credentials("demo", "secret")).refreshToken().token();

            AuthenticationResult refreshed = service.refresh(firstRefreshToken);

            assertThat(refreshed.refreshToken().token()).isEqualTo("token-2").isNotEqualTo(firstRefreshToken);
            assertThat(refreshed.accessToken().value()).isEqualTo("access-for-demo");
            assertThat(refreshTokens.findByToken(firstRefreshToken))
                    .as("the used refresh token must not survive rotation")
                    .isEmpty();
        }

        @Test
        void rejectsExpiredToken() {
            refreshTokens.save(new RefreshToken(demoUser.id(), "stale", NOW.minusSeconds(1)));

            assertThatThrownBy(() -> service.refresh("stale")).isInstanceOf(InvalidCredentialsException.class);
            assertThat(refreshTokens.findByToken("stale")).isEmpty();
        }

    }

    @Nested
    class Logout {

        @Test
        void revokesRefreshToken() {
            String refreshToken =
                    service.login(new Credentials("demo", "secret")).refreshToken().token();

            service.logout(refreshToken);

            assertThat(refreshTokens.findByToken(refreshToken)).isEmpty();
            assertThatThrownBy(() -> service.refresh(refreshToken))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

    }

    private static final class InMemoryUserRepository implements UserRepository {

        private final Map<UUID, User> byId = new HashMap<>();

        private InMemoryUserRepository(User... seeded) {
            for (User user : seeded) {
                byId.put(user.id(), user);
            }
        }

        @Override
        public Optional<User> findByUsername(String username) {
            return byId.values().stream().filter(user -> user.username().equalsIgnoreCase(username)).findFirst();
        }

        @Override
        public Optional<User> findById(UUID id) {
            return Optional.ofNullable(byId.get(id));
        }
    }

    private static final class InMemoryRefreshTokenStore implements RefreshTokenStore {

        private final Map<String, RefreshToken> tokens = new HashMap<>();

        @Override
        public void save(RefreshToken refreshToken) {
            tokens.put(refreshToken.token(), refreshToken);
        }

        @Override
        public Optional<RefreshToken> findByToken(String token) {
            return Optional.ofNullable(tokens.get(token));
        }

        @Override
        public void delete(String token) {
            tokens.remove(token);
        }
    }

    private static final class PrefixPasswordHasher implements PasswordHasher {

        @Override
        public String hash(String rawPassword) {
            return "hashed:" + rawPassword;
        }

        @Override
        public boolean matches(String rawPassword, String passwordHash) {
            return hash(rawPassword).equals(passwordHash);
        }
    }

    private static final class StubAccessTokenIssuer implements AccessTokenIssuer {

        @Override
        public AccessToken issue(User user) {
            return new AccessToken("access-for-" + user.username(), NOW.plus(Duration.ofMinutes(15)));
        }
    }

    private static final class SequentialTokenGenerator
            implements com.contactmanager.domain.port.out.RefreshTokenGenerator {

        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public String newToken() {
            return "token-" + counter.incrementAndGet();
        }
    }
}
