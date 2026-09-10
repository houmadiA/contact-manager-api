package com.contactmanager.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.contactmanager.domain.model.Contact;
import com.contactmanager.domain.model.ContactSortField;
import com.contactmanager.domain.model.PageQuery;
import com.contactmanager.domain.model.PageResult;
import com.contactmanager.domain.model.SortDirection;
import com.contactmanager.domain.model.SortSpec;
import com.contactmanager.infrastructure.support.PostgresIntegrationTest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@EntityScan(basePackageClasses = ContactEntity.class)
@EnableJpaRepositories(basePackageClasses = ContactEntityRepository.class)
@Import({ContactPersistenceAdapter.class, ContactPersistenceMapperImpl.class})
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=none")
class ContactPersistenceAdapterIT extends PostgresIntegrationTest {

    private static final Instant NOW = Instant.parse("2026-03-01T09:00:00Z");

    @Autowired
    private ContactPersistenceAdapter adapter;

    @Autowired
    private ContactEntityRepository jpaRepository;

    @BeforeEach
    void clearContacts() {
        jpaRepository.deleteAll();
    }

    @Test
    void roundTripsEveryField() {
        Contact contact = new Contact(
                UUID.randomUUID(),
                "Ada",
                "Lovelace",
                "ada@example.com",
                "+44 20 7946 0000",
                "Analytical Engines",
                "Mathematician",
                "12 Marylebone Road, London",
                NOW,
                NOW);

        adapter.save(contact);

        assertThat(adapter.findById(contact.id())).contains(contact);
    }

    @Test
    void reportsDeleteOutcome() {
        Contact contact = adapter.save(contact("Ada", "Lovelace", "ada@example.com", "Analytical"));

        assertThat(adapter.deleteById(contact.id())).isTrue();
        assertThat(adapter.deleteById(contact.id())).isFalse();
        assertThat(adapter.findById(contact.id())).isEmpty();
    }

    @Test
    void matchesFreeTextTerm() {
        seedThree();

        assertThat(searchFor("lovel")).containsExactly("Lovelace");
        assertThat(searchFor("GRACE@")).containsExactly("Hopper");
        assertThat(searchFor("analytical")).containsExactlyInAnyOrder("Lovelace", "Turing");
        assertThat(searchFor("nothing here")).isEmpty();
    }

    @Test
    void sortsOnRequestedField() {
        seedThree();

        PageResult<Contact> descending = adapter.search(
                null,
                new PageQuery(0, 10, new SortSpec(ContactSortField.LAST_NAME, SortDirection.DESC)));

        assertThat(descending.content()).extracting(Contact::lastName)
                .containsExactly("Turing", "Lovelace", "Hopper");
    }

    @Test
    void returnsUnpagedFilteredSet() {
        seedThree();

        List<Contact> exported = adapter.findAll(
                "analytical",
                new SortSpec(ContactSortField.FIRST_NAME, SortDirection.ASC));

        assertThat(exported).extracting(Contact::firstName).containsExactly("Ada", "Alan");
    }

    private List<String> searchFor(String term) {
        return adapter.search(term, PageQuery.firstPage(20)).content().stream()
                .map(Contact::lastName)
                .toList();
    }

    private void seedThree() {
        adapter.save(contact("Ada", "Lovelace", "ada@example.com", "Analytical"));
        adapter.save(contact("Grace", "Hopper", "grace@example.com", "Navy"));
        adapter.save(contact("Alan", "Turing", "alan@example.com", "Analytical"));
    }

    private static Contact contact(String firstName, String lastName, String email, String company) {
        return new Contact(
                UUID.randomUUID(), firstName, lastName, email, null, company, null, null, NOW, NOW);
    }
}
