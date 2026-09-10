package com.contactmanager.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.contactmanager.domain.exception.InvalidContactException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ContactTest {

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-02-02T11:00:00Z");

    @Test
    void trimsAndDropsBlanks() {
        Contact contact = contact("  Ada ", " Lovelace ", "ada@example.com", "   ", "  Analytical Engines ", null);

        assertThat(contact.firstName()).isEqualTo("Ada");
        assertThat(contact.lastName()).isEqualTo("Lovelace");
        assertThat(contact.company()).isEqualTo("Analytical Engines");
        assertThat(contact.phoneNumber()).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void requiresFirstName(String candidate) {
        assertThatThrownBy(() -> contact(candidate, "Lovelace", "ada@example.com", null, null, null))
                .isInstanceOf(InvalidContactException.class)
                .hasMessageContaining("firstName");
    }

    @Test
    void rejectsOverlongAddress() {
        assertThatThrownBy(() -> contact("Ada", "Lovelace", "ada@example.com", null, null, "a".repeat(256)))
                .isInstanceOf(InvalidContactException.class)
                .hasMessageContaining("address");
    }

    @Nested
    class Email {

        @Test
        void normalisesCaseAndWhitespace() {
            assertThat(contact("Ada", "Lovelace", "  Ada.Lovelace@Example.COM  ", null, null, null).email())
                    .isEqualTo("ada.lovelace@example.com");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "ada", "ada@", "@example.com", "ada@example", "ada lovelace@example.com"})
        void rejectsInvalidAddresses(String candidate) {
            assertThatThrownBy(() -> contact("Ada", "Lovelace", candidate, null, null, null))
                    .isInstanceOf(InvalidContactException.class)
                    .hasMessageContaining("email");
        }
    }

    @Test
    void updateKeepsIdentityAndCreationDate() {
        UUID id = UUID.randomUUID();
        Contact stored = new Contact(
                id, "Ada", "Lovelace", "ada@example.com", null, null, null, null, CREATED_AT, CREATED_AT);

        Contact updated = stored.withDetailsOf(
                contact("Grace", "Hopper", "grace@example.com", null, null, null), UPDATED_AT);

        assertThat(updated.id()).isEqualTo(id);
        assertThat(updated.createdAt()).isEqualTo(CREATED_AT);
        assertThat(updated.updatedAt()).isEqualTo(UPDATED_AT);
        assertThat(updated.firstName()).isEqualTo("Grace");
        assertThat(stored.firstName()).as("the original is untouched").isEqualTo("Ada");
    }

    /** A contact as it arrives from a request or a CSV row: no identity, no timestamps yet. */
    private static Contact contact(
            String firstName, String lastName, String email, String phone, String company, String address) {
        return new Contact(null, firstName, lastName, email, phone, company, null, address, null, null);
    }
}
