package com.contactmanager.infrastructure.csv;

import com.contactmanager.domain.exception.CsvImportException;
import com.contactmanager.domain.model.RawContactRow;
import com.contactmanager.domain.port.out.ContactCsvReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

@Component
public class CommonsCsvContactReader implements ContactCsvReader {

    private static final int BYTE_ORDER_MARK = 0xFEFF;

    @Override
    public List<RawContactRow> read(InputStream inputStream) {
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                CSVParser parser = CSVFormat.DEFAULT
                        .builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setIgnoreHeaderCase(true)
                        .setIgnoreEmptyLines(true)
                        .setIgnoreSurroundingSpaces(true)
                        .setTrim(true)
                        .get()
                        .parse(stripByteOrderMark(reader))) {

            requireColumns(parser);

            List<RawContactRow> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                rows.add(toRow(record));
            }
            return rows;
        } catch (IOException | UncheckedIOException e) {
            throw new CsvImportException("Could not read the CSV file: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new CsvImportException("The CSV file is malformed: " + e.getMessage(), e);
        }
    }

    private static void requireColumns(CSVParser parser) {
        List<String> headers = parser.getHeaderNames().stream()
                .map(header -> header.trim().toLowerCase(Locale.ROOT))
                .toList();

        List<String> missing = ContactCsvFormat.REQUIRED_HEADERS.stream()
                .filter(required -> !headers.contains(required.toLowerCase(Locale.ROOT)))
                .toList();

        if (!missing.isEmpty()) {
            throw new CsvImportException("The CSV file is missing required column(s): " + String.join(", ", missing));
        }
    }

    private static RawContactRow toRow(CSVRecord record) {
        return new RawContactRow(
                (int) record.getRecordNumber() + 1, // +1: the header occupies the first line
                value(record, ContactCsvFormat.FIRST_NAME),
                value(record, ContactCsvFormat.LAST_NAME),
                value(record, ContactCsvFormat.EMAIL),
                value(record, ContactCsvFormat.PHONE_NUMBER),
                value(record, ContactCsvFormat.COMPANY),
                value(record, ContactCsvFormat.JOB_TITLE),
                value(record, ContactCsvFormat.ADDRESS));
    }

    private static String value(CSVRecord record, String column) {
        if (!record.isMapped(column) || !record.isSet(column)) {
            return null;
        }
        String value = record.get(column);
        return value == null || value.isBlank() ? null : value;
    }

    private static Reader stripByteOrderMark(Reader reader) throws IOException {
        reader.mark(1);
        int first = reader.read();
        if (first != BYTE_ORDER_MARK) {
            reader.reset();
        }
        return reader;
    }
}
