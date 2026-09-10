package com.contactmanager;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
@TestPropertySource(
        properties = {
            "contact-manager.jwt.secret=an-integration-test-secret-that-is-long-enough",
            "contact-manager.jwt.access-token-ttl=15m"
        })
class ContactManagerIT {

    private static final int SEEDED_CONTACTS = 50;
    private static final String ACCESS_TOKEN_COOKIE = "cm_access_token";
    private static final String REFRESH_TOKEN_COOKIE = "cm_refresh_token";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private TestRestTemplate rest;

    @Nested
    class Authentication {

        @Test
        void signsInWithSeededAccount() {
            ResponseEntity<Map> response = login();

            assertThat(response.getBody()).containsEntry("username", "demo");
            assertThat(response.getBody().keySet())
                    .as("the tokens travel in cookies, never in a body a script could read")
                    .doesNotContain("accessToken", "refreshToken");
            assertThat(response.getHeaders().get(HttpHeaders.SET_COOKIE))
                    .hasSize(2)
                    .allMatch(cookie -> cookie.contains("HttpOnly"))
                    .allMatch(cookie -> cookie.contains("Secure"))
                    .allMatch(cookie -> cookie.contains("SameSite=Strict"));
            assertThat(cookieValue(response, ACCESS_TOKEN_COOKIE)).isNotBlank();
            assertThat(cookieValue(response, REFRESH_TOKEN_COOKIE)).isNotBlank();
        }

        @Test
        void rotatesRefreshToken() {
            String refreshToken = cookieValue(login(), REFRESH_TOKEN_COOKIE);

            ResponseEntity<Map> refreshed = refresh(refreshToken);

            assertThat(refreshed.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(cookieValue(refreshed, REFRESH_TOKEN_COOKIE)).isNotEqualTo(refreshToken);
            assertThat(cookieValue(refreshed, ACCESS_TOKEN_COOKIE)).isNotBlank();

            assertThat(refresh(refreshToken).getStatusCode())
                    .as("a rotated refresh token must not work twice")
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void refusesToRefreshWithoutTheCookie() {
            assertThat(rest.exchange("/api/auth/refresh", HttpMethod.POST, null, Map.class)
                            .getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void servesTheSessionFromTheAccessTokenCookieAlone() {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.COOKIE, ACCESS_TOKEN_COOKIE + "=" + cookieValue(login(), ACCESS_TOKEN_COOKIE));

            ResponseEntity<Map> me =
                    rest.exchange("/api/auth/me", HttpMethod.GET, new HttpEntity<>(headers), Map.class);

            assertThat(me.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(me.getBody()).containsEntry("username", "demo");
        }

        @Test
        void expiresBothCookiesOnLogout() {
            ResponseEntity<Void> loggedOut = rest.exchange(
                    "/api/auth/logout", HttpMethod.POST, new HttpEntity<>(authorised()), Void.class);

            assertThat(loggedOut.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(loggedOut.getHeaders().get(HttpHeaders.SET_COOKIE))
                    .hasSize(2)
                    .allMatch(cookie -> cookie.contains("Max-Age=0"));
        }
    }

    @Nested
    class Contacts {

        @Test
        void listsSeededContacts() {
            ResponseEntity<Map> page = get("/api/contacts?page=0&size=10&sort=lastName,asc");

            assertThat(page.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat((List<?>) page.getBody().get("content")).hasSize(10);
            assertThat((Integer) page.getBody().get("totalElements")).isGreaterThanOrEqualTo(SEEDED_CONTACTS);
        }

        @Test
        void performsFullLifecycle() {
            ResponseEntity<Map> created = post(
                    "/api/contacts",
                    Map.of(
                            "firstName", "Integration",
                            "lastName", "Tester",
                            "email", "integration.tester@example.com",
                            "company", "Contact Manager"));

            assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            String id = (String) created.getBody().get("id");
            assertThat(created.getHeaders().getLocation()).hasToString("/api/contacts/" + id);

            assertThat(get("/api/contacts/" + id).getBody()).containsEntry("fullName", "Integration Tester");

            ResponseEntity<Map> updated = exchange(
                    "/api/contacts/" + id,
                    HttpMethod.PUT,
                    Map.of(
                            "firstName", "Integration",
                            "lastName", "Tester",
                            "email", "integration.tester@example.com",
                            "jobTitle", "Automation Lead"));
            assertThat(updated.getBody()).containsEntry("jobTitle", "Automation Lead");

            assertThat(exchange("/api/contacts/" + id, HttpMethod.DELETE, null).getStatusCode())
                    .isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(get("/api/contacts/" + id).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    class Csv {

        @Test
        void importsThenExports() {
            String csv =
                    """
                    firstName,lastName,email,company
                    Imported,Contact,imported.contact@example.com,CSV Import Ltd
                    Broken,Row,not-an-email,CSV Import Ltd
                    """;

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(csv.getBytes(StandardCharsets.UTF_8)) {
                @Override
                public String getFilename() {
                    return "contacts.csv";
                }
            });

            HttpHeaders headers = authorised();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            ResponseEntity<Map> report = rest.postForEntity(
                    "/api/contacts/import", new HttpEntity<>(body, headers), Map.class);

            assertThat(report.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(report.getBody()).containsEntry("imported", 1).containsEntry("skipped", 1);
            assertThat((List<?>) report.getBody().get("errors")).hasSize(1);

            ResponseEntity<String> export = rest.exchange(
                    "/api/contacts/export?search=imported.contact",
                    HttpMethod.GET,
                    new HttpEntity<>(authorised()),
                    String.class);

            assertThat(export.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(export.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                    .contains("attachment")
                    .contains("contacts.csv");
            assertThat(export.getBody())
                    .contains("firstName,lastName,email")
                    .contains("imported.contact@example.com")
                    .doesNotContain("not-an-email");
        }
    }

    private ResponseEntity<Map> login() {
        ResponseEntity<Map> response = rest.postForEntity(
                "/api/auth/login", Map.of("username", "demo", "password", "demo1234"), Map.class);

        assertThat(response.getStatusCode())
                .as("the seeded demo account must be able to sign in")
                .isEqualTo(HttpStatus.OK);
        return response;
    }

    private ResponseEntity<Map> refresh(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, REFRESH_TOKEN_COOKIE + "=" + refreshToken);
        return rest.exchange("/api/auth/refresh", HttpMethod.POST, new HttpEntity<>(headers), Map.class);
    }

    private HttpHeaders authorised() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, sessionCookies(login()));
        return headers;
    }

    private static String sessionCookies(ResponseEntity<?> response) {
        return response.getHeaders().get(HttpHeaders.SET_COOKIE).stream()
                .map(ContactManagerIT::nameAndValue)
                .collect(Collectors.joining("; "));
    }

    private static String cookieValue(ResponseEntity<?> response, String name) {
        return response.getHeaders().get(HttpHeaders.SET_COOKIE).stream()
                .filter(cookie -> cookie.startsWith(name + "="))
                .map(cookie -> nameAndValue(cookie).substring(name.length() + 1))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no " + name + " cookie was set"));
    }

    private static String nameAndValue(String setCookie) {
        int end = setCookie.indexOf(';');
        return end < 0 ? setCookie : setCookie.substring(0, end);
    }

    private ResponseEntity<Map> get(String path) {
        return rest.exchange(path, HttpMethod.GET, new HttpEntity<>(authorised()), Map.class);
    }

    private ResponseEntity<Map> post(String path, Object payload) {
        return exchange(path, HttpMethod.POST, payload);
    }

    private ResponseEntity<Map> exchange(String path, HttpMethod method, Object payload) {
        HttpHeaders headers = authorised();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.exchange(path, method, new HttpEntity<>(payload, headers), Map.class);
    }
}
