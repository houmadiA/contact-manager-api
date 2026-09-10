package com.contactmanager.domain.service;

import static com.contactmanager.domain.support.ContactFixtures.contact;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.contactmanager.domain.exception.ContactNotFoundException;
import com.contactmanager.domain.exception.DuplicateEmailException;
import com.contactmanager.domain.model.Contact;
import com.contactmanager.domain.model.ContactSortField;
import com.contactmanager.domain.model.PageQuery;
import com.contactmanager.domain.model.PageResult;
import com.contactmanager.domain.model.SortDirection;
import com.contactmanager.domain.model.SortSpec;
import com.contactmanager.domain.port.in.ContactService;
import com.contactmanager.domain.support.ContactFixtures;
import com.contactmanager.domain.support.InMemoryContactRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ContactServiceTest {

    private static final Instant NOW = Instant.parse("2026-03-01T09:00:00Z");
    private static final Instant LATER = Instant.parse("2026-03-02T09:00:00Z");

    private InMemoryContactRepository repository;
    private MutableClock clock;
    private ContactService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryContactRepository();
        clock = new MutableClock(NOW);
        service = new ContactServiceImpl(repository, ContactFixtures.noCsvReader(), (contacts, out) -> {}, clock);
    }

    @Nested
    class Create {

        @Test
        void assignsIdentityAndTimestamps() {
            Contact created = service.create(contact("Ada", "Lovelace", "ada@example.com"));

            assertThat(created.id()).isNotNull();
            assertThat(created.createdAt()).isEqualTo(NOW);
            assertThat(created.updatedAt()).isEqualTo(NOW);
            assertThat(repository.findById(created.id())).contains(created);
        }

        @Test
        void refusesDuplicateEmail() {
            service.create(contact("Ada", "Lovelace", "ada@example.com"));

            assertThatThrownBy(() -> service.create(contact("Augusta", "King", "ADA@example.com")))
                    .isInstanceOf(DuplicateEmailException.class)
                    .hasMessageContaining("ada@example.com");

            assertThat(repository.size()).isEqualTo(1);
        }
    }

    @Nested
    class Update {

        @Test
        void replacesDetailsAndStampsTheChange() {
            Contact created = service.create(contact("Ada", "Lovelace", "ada@example.com"));
            clock.set(LATER);

            Contact updated =
                    service.update(created.id(), contact("Ada", "Lovelace", "ada@example.com", "Countess"));

            assertThat(updated.jobTitle()).isEqualTo("Countess");
            assertThat(updated.createdAt()).isEqualTo(NOW);
            assertThat(updated.updatedAt()).isEqualTo(LATER);
        }

        @Test
        void refusesEmailOwnedByAnother() {
            service.create(contact("Ada", "Lovelace", "ada@example.com"));
            Contact grace = service.create(contact("Grace", "Hopper", "grace@example.com"));

            assertThatThrownBy(() -> service.update(grace.id(), contact("Grace", "Hopper", "ada@example.com")))
                    .isInstanceOf(DuplicateEmailException.class);
        }

        @Test
        void failsForUnknownContact() {
            assertThatThrownBy(
                            () -> service.update(UUID.randomUUID(), contact("Ada", "Lovelace", "ada@example.com")))
                    .isInstanceOf(ContactNotFoundException.class);
        }
    }

    @Test
    void failsToDeleteUnknownContact() {
        assertThatThrownBy(() -> service.delete(UUID.randomUUID())).isInstanceOf(ContactNotFoundException.class);
    }

    @Nested
    class List {

        @BeforeEach
        void seed() {
            service.create(contact("Ada", "Lovelace", "ada@example.com", null, "Analytical"));
            service.create(contact("Grace", "Hopper", "grace@example.com", null, "Navy"));
            service.create(contact("Alan", "Turing", "alan@example.com", null, "Analytical"));
        }

        @Test
        void sortsByRequestedField() {
            PageResult<Contact> page = service.list(
                    null, new PageQuery(0, 10, new SortSpec(ContactSortField.LAST_NAME, SortDirection.DESC)));

            assertThat(page.content())
                    .extracting(Contact::lastName)
                    .containsExactly("Turing", "Lovelace", "Hopper");
        }

        @Test
        void filtersOnFreeTextTerm() {
            PageResult<Contact> page = service.list("analytical", PageQuery.firstPage(20));

            assertThat(page.content()).extracting(Contact::firstName).containsExactlyInAnyOrder("Ada", "Alan");
            assertThat(page.totalElements()).isEqualTo(2);
        }

        @Test
        void reportsPagingMetadata() {
            PageResult<Contact> page = service.list(
                    null, new PageQuery(1, 2, new SortSpec(ContactSortField.FIRST_NAME, SortDirection.ASC)));

            assertThat(page.content()).extracting(Contact::firstName).containsExactly("Grace");
            assertThat(page.page()).isEqualTo(1);
            assertThat(page.totalElements()).isEqualTo(3);
            assertThat(page.totalPages()).isEqualTo(2);
        }
    }

    private static final class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        void set(Instant newInstant) {
            this.instant = newInstant;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }
    }
}
