package com.contactmanager.application.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.contactmanager.application.mapper.ContactWebMapperImpl;
import com.contactmanager.application.security.AuthCookieFactory;
import com.contactmanager.application.security.AuthCookieProperties;
import com.contactmanager.application.security.CookieBearerTokenResolver;
import com.contactmanager.domain.exception.InvalidCredentialsException;
import com.contactmanager.domain.model.AccessToken;
import com.contactmanager.domain.model.AuthenticationResult;
import com.contactmanager.domain.model.Credentials;
import com.contactmanager.domain.model.RefreshToken;
import com.contactmanager.domain.model.Role;
import com.contactmanager.domain.model.User;
import com.contactmanager.domain.port.in.AuthenticationService;
import com.contactmanager.domain.port.out.UserRepository;
import com.contactmanager.infrastructure.config.SecurityConfig;
import jakarta.servlet.http.Cookie;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(AuthController.class)
@Import({
    SecurityConfig.class,
    ApiExceptionHandler.class,
    ContactWebMapperImpl.class,
    AuthCookieFactory.class,
    CookieBearerTokenResolver.class,
    AuthControllerTest.FixedClock.class
})
@EnableConfigurationProperties(AuthCookieProperties.class)
class AuthControllerTest {

    private static final UUID USER_ID = UUID.fromString("99999999-8888-7777-6666-555555555555");
    private static final Instant NOW = Instant.parse("2026-03-01T09:00:00Z");
    private static final Instant ACCESS_EXPIRES_AT = NOW.plusSeconds(900);
    private static final Instant REFRESH_EXPIRES_AT = NOW.plusSeconds(604_800);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authentication;

    @MockitoBean
    private UserRepository users;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void handsTheTokensOverAsHardenedCookies() throws Exception {
        given(authentication.login(new Credentials("demo", "secret"))).willReturn(result("refresh-token"));

        MvcResult response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"demo","password":"secret"}"""))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(setCookie(response, AuthCookieFactory.ACCESS_TOKEN))
                .contains("cm_access_token=access-token")
                .contains("Max-Age=900")
                .contains("Path=/api;");
        assertThat(setCookie(response, AuthCookieFactory.REFRESH_TOKEN))
                .contains("cm_refresh_token=refresh-token")
                .contains("Max-Age=604800")
                .contains("Path=/api/auth;");
        assertThat(setCookies(response))
                .as("a token in a cookie is only as safe as the flags around it")
                .allMatch(cookie -> cookie.contains("HttpOnly"))
                .allMatch(cookie -> cookie.contains("Secure"))
                .allMatch(cookie -> cookie.contains("SameSite=Strict"));
    }

    @Test
    void keepsTheTokensOutOfTheResponseBody() throws Exception {
        given(authentication.login(any())).willReturn(result("refresh-token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"demo","password":"secret"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(jsonPath("$.username").value("demo"))
                .andExpect(jsonPath("$.displayName").value("Demo User"))
                .andExpect(jsonPath("$.roles[0]").value("USER"));
    }

    @Test
    void answers401ForBadCredentials() throws Exception {
        given(authentication.login(any())).willThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"demo","password":"wrong"}"""))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid username or password"));
    }

    @Test
    void rotatesTheRefreshTokenReadFromItsCookie() throws Exception {
        given(authentication.refresh("refresh-token")).willReturn(result("rotated-token"));

        MvcResult response = mockMvc.perform(
                        post("/api/auth/refresh").cookie(new Cookie(AuthCookieFactory.REFRESH_TOKEN, "refresh-token")))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(setCookie(response, AuthCookieFactory.REFRESH_TOKEN)).contains("cm_refresh_token=rotated-token");
    }

    @Test
    void answers401WhenTheRefreshCookieIsMissing() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")).andExpect(status().isUnauthorized());

        then(authentication).should(never()).refresh(any());
    }

    @Test
    void revokesTheRefreshTokenAndExpiresBothCookiesOnLogout() throws Exception {
        MvcResult response = mockMvc.perform(
                        post("/api/auth/logout").cookie(new Cookie(AuthCookieFactory.REFRESH_TOKEN, "refresh-token")))
                .andExpect(status().isNoContent())
                .andReturn();

        assertThat(setCookies(response)).allMatch(cookie -> cookie.contains("Max-Age=0"));
        assertThat(setCookie(response, AuthCookieFactory.ACCESS_TOKEN)).contains("cm_access_token=;");
        assertThat(setCookie(response, AuthCookieFactory.REFRESH_TOKEN)).contains("cm_refresh_token=;");
        then(authentication).should().logout("refresh-token");
    }

    @Test
    void expiresTheCookiesEvenWithoutARefreshToken() throws Exception {
        MvcResult response =
                mockMvc.perform(post("/api/auth/logout")).andExpect(status().isNoContent()).andReturn();

        assertThat(setCookies(response)).hasSize(2);
        then(authentication).should(never()).logout(any());
    }

    @Test
    void refusesAnonymousMe() throws Exception {
        mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    private static List<String> setCookies(MvcResult response) {
        return response.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
    }

    private static String setCookie(MvcResult response, String name) {
        return setCookies(response).stream()
                .filter(cookie -> cookie.startsWith(name + "="))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no Set-Cookie header for " + name));
    }

    private static AuthenticationResult result(String refreshToken) {
        return new AuthenticationResult(
                new AccessToken("access-token", ACCESS_EXPIRES_AT),
                new RefreshToken(USER_ID, refreshToken, REFRESH_EXPIRES_AT),
                demoUser());
    }

    private static User demoUser() {
        return new User(USER_ID, "demo", "hashed-secret", "Demo User", Set.of(Role.USER));
    }

    @TestConfiguration
    static class FixedClock {

        @Bean
        Clock clock() {
            return Clock.fixed(NOW, ZoneOffset.UTC);
        }
    }
}
