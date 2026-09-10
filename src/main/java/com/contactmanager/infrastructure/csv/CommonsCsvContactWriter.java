package com.contactmanager.infrastructure.csv;

import com.contactmanager.domain.model.Contact;
import com.contactmanager.domain.port.out.ContactCsvWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

@Component
public class CommonsCsvContactWriter implements ContactCsvWriter {

    @Override
    public void write(List<Contact> contacts, OutputStream outputStream) {
        Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        try {
            writer.write(ContactCsvFormat.BYTE_ORDER_MARK);

            try (CSVPrinter printer = new CSVPrinter(
                    writer, CSVFormat.DEFAULT.builder().setHeader(ContactCsvFormat.HEADERS).get())) {
                for (Contact contact : contacts) {
                    printer.printRecord(
                            contact.firstName(),
                            contact.lastName(),
                            contact.email(),
                            contact.phoneNumber(),
                            contact.company(),
                            contact.jobTitle(),
                            contact.address());
                }
                printer.flush();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write the CSV export", e);
        }
    }
}
