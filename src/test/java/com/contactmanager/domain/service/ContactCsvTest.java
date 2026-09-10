package com.contactmanager.domain.service;

import static com.contactmanager.domain.support.ContactFixtures.contact;
import static com.contactmanager.domain.support.ContactFixtures.row;
import static org.assertj.core.api.Assertions.assertThat;

import com.contactmanager.domain.model.Contact;
import com.contactmanager.domain.model.ContactSortField;
import com.contactmanager.domain.model.ImportReport;
import com.contactmanager.domain.model.RawContactRow;
import com.contactmanager.domain.model.SortDirection;
import com.contactmanager.domain.model.SortSpec;
import com.contactmanager.domain.port.in.ContactService;
import com.contactmanager.domain.port.out.ContactCsvReader;
import com.contactmanager.domain.support.InMemoryContactRepository;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ContactCsvTest {

    private static final InputStream IRRELEVANT = new ByteArrayInputStream(new byte[0]);

    private InMemoryContactRepository repository;
    private StubCsvReader csvReader;
    private CapturingCsvWriter csvWriter;
    private ContactService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryContactRepository();
        csvReader = new StubCsvReader();
        csvWriter = new CapturingCsvWriter();
        Clock clock = Clock.fixed(Instant.parse("2026-03-01T09:00:00Z"), ZoneOffset.UTC);
        service = new ContactServiceImpl(repository, csvReader, csvWriter, clock);
    }

    @Nested
    class Import {

        @Test
        void importsValidRows() {
            csvReader.returns(
                    row(2, "Ada", "Lovelace", "ada@example.com"), row(3, "Grace", "Hopper", "grace@example.com"));

            ImportReport report = service.importFromCsv(IRRELEVANT);

            assertThat(report.imported()).isEqualTo(2);
            assertThat(report.skipped()).isZero();
            assertThat(report.errors()).isEmpty();
            assertThat(repository.size()).isEqualTo(2);
        }

        @Test
        void skipsInvalidRowAndContinues() {
            csvReader.returns(
                    row(2, "Ada", "Lovelace", "ada@example.com"),
                    row(3, "", "Hopper", "grace@example.com"),
                    row(4, "Alan", "Turing", "not-an-email"),
                    row(5, "Katherine", "Johnson", "katherine@example.com"));

            ImportReport report = service.importFromCsv(IRRELEVANT);

            assertThat(report.imported()).isEqualTo(2);
            assertThat(report.skipped()).isEqualTo(2);
            assertThat(report.errors()).extracting(ImportReport.ImportError::lineNumber).containsExactly(3, 4);
            assertThat(report.errors().get(0).message()).contains("firstName");
            assertThat(report.errors().get(1).message()).contains("email");
            assertThat(repository.size()).isEqualTo(2);
        }

        @Test
        void skipsEmailAlreadyStored() {
            csvReader.returns(row(2, "Ada", "Lovelace", "ada@example.com"));
            service.importFromCsv(IRRELEVANT);

            csvReader.returns(row(2, "Augusta", "King", "ADA@example.com"));
            ImportReport report = service.importFromCsv(IRRELEVANT);

            assertThat(report.imported()).isZero();
            assertThat(report.skipped()).isEqualTo(1);
            assertThat(report.errors().get(0).message()).contains("already exists");
            assertThat(repository.size()).isEqualTo(1);
        }

        @Test
        void reportsEmptyFile() {
            csvReader.returns();

            ImportReport report = service.importFromCsv(IRRELEVANT);

            assertThat(report.imported()).isZero();
            assertThat(report.total()).isZero();
        }
    }

    @Nested
    class Export {

        @BeforeEach
        void seed() {
            service.create(contact("Ada", "Lovelace", "ada@example.com", null, "Analytical"));
            service.create(contact("Grace", "Hopper", "grace@example.com", null, "Navy"));
            service.create(contact("Alan", "Turing", "alan@example.com", null, "Analytical"));
        }

        @Test
        void writesEveryContact() {
            service.exportToCsv(null, SortSpec.byDefault(), OutputStream.nullOutputStream());

            assertThat(csvWriter.written)
                    .extracting(Contact::lastName)
                    .containsExactly("Hopper", "Lovelace", "Turing");
        }

        @Test
        void writesTheFilteredAndSortedSet() {
            service.exportToCsv(
                    "analytical",
                    new SortSpec(ContactSortField.FIRST_NAME, SortDirection.DESC),
                    OutputStream.nullOutputStream());

            assertThat(csvWriter.written).extracting(Contact::firstName).containsExactly("Alan", "Ada");
        }
    }

    private static final class StubCsvReader implements ContactCsvReader {

        private final List<RawContactRow> rows = new ArrayList<>();

        void returns(RawContactRow... newRows) {
            rows.clear();
            rows.addAll(List.of(newRows));
        }

        @Override
        public List<RawContactRow> read(InputStream inputStream) {
            assertThat(inputStream).isNotNull();
            return List.copyOf(rows);
        }
    }

    private static final class CapturingCsvWriter implements com.contactmanager.domain.port.out.ContactCsvWriter {

        private List<Contact> written = List.of();

        @Override
        public void write(List<Contact> contacts, OutputStream outputStream) {
            this.written = contacts;
        }
    }
}
