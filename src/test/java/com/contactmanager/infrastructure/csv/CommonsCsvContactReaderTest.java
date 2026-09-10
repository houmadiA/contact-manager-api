package com.contactmanager.infrastructure.csv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.contactmanager.domain.exception.CsvImportException;
import com.contactmanager.domain.model.RawContactRow;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class CommonsCsvContactReaderTest {

    private final CommonsCsvContactReader reader = new CommonsCsvContactReader();

    @Test
    void readsDocumentedColumns() {
        List<RawContactRow> rows = read("""
                firstName,lastName,email,phoneNumber,company,jobTitle,address
                Ada,Lovelace,ada@example.com,+44 20 7946 0000,Analytical Engines,Mathematician,London
                """);

        assertThat(rows).hasSize(1);
        RawContactRow row = rows.get(0);
        assertThat(row.firstName()).isEqualTo("Ada");
        assertThat(row.lastName()).isEqualTo("Lovelace");
        assertThat(row.email()).isEqualTo("ada@example.com");
        assertThat(row.phoneNumber()).isEqualTo("+44 20 7946 0000");
        assertThat(row.company()).isEqualTo("Analytical Engines");
        assertThat(row.jobTitle()).isEqualTo("Mathematician");
        assertThat(row.address()).isEqualTo("London");
    }

    @Test
    void reportsLineNumbers() {
        List<RawContactRow> rows = read("""
                firstName,lastName,email
                Ada,Lovelace,ada@example.com
                Grace,Hopper,grace@example.com
                """);

        assertThat(rows).extracting(RawContactRow::lineNumber).containsExactly(2, 3);
    }

    @Test
    void acceptsRequiredColumnsOnly() {
        List<RawContactRow> rows = read("""
                firstName,lastName,email
                Ada,Lovelace,ada@example.com
                """);

        assertThat(rows.get(0).company()).isNull();
        assertThat(rows.get(0).address()).isNull();
    }

    @Test
    void handlesQuotedValues() {
        List<RawContactRow> rows = read("""
                firstName,lastName,email,company,address
                Ada,Lovelace,ada@example.com,"Babbage, ""Analytical"" Engines","12 Marylebone Road, London"
                """);

        assertThat(rows.get(0).company()).isEqualTo("Babbage, \"Analytical\" Engines");
        assertThat(rows.get(0).address()).isEqualTo("12 Marylebone Road, London");
    }

    @Test
    void rejectsMissingRequiredColumn() {
        assertThatThrownBy(() -> read("firstName,lastName\nAda,Lovelace\n"))
                .isInstanceOf(CsvImportException.class)
                .hasMessageContaining("email");
    }

    private List<RawContactRow> read(String csv) {
        InputStream input = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        return reader.read(input);
    }
}
