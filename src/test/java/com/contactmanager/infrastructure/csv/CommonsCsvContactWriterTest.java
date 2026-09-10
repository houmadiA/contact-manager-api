package com.contactmanager.infrastructure.csv;

import static org.assertj.core.api.Assertions.assertThat;

import com.contactmanager.domain.model.Contact;
import com.contactmanager.domain.model.RawContactRow;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CommonsCsvContactWriterTest {

    private static final Instant NOW = Instant.parse("2026-03-01T09:00:00Z");

    private final CommonsCsvContactWriter writer = new CommonsCsvContactWriter();

    @Test
    void writesOneLinePerContact() {
        String csv = write(List.of(
                contact("Ada", "Lovelace", "ada@example.com", null, "Analytical Engines", null, null)));

        assertThat(csv.lines().toList().get(1))
                .isEqualTo("Ada,Lovelace,ada@example.com,,Analytical Engines,,");
    }

    @Test
    void roundTripsAwkwardValuesThroughTheReader() {
        Contact amelie = contact(
                "Amélie",
                "Poulain",
                "amelie@example.com",
                "+33 1 23 45 67 89",
                "Babbage, \"Analytical\" Engines",
                "Serveuse",
                "15 Rue Lepic, Paris");

        byte[] exported = writeBytes(List.of(amelie));

        assertThat(new String(exported, StandardCharsets.UTF_8))
                .contains("\"Babbage, \"\"Analytical\"\" Engines\"")
                .contains("\"15 Rue Lepic, Paris\"");

        List<RawContactRow> rows = new CommonsCsvContactReader().read(new ByteArrayInputStream(exported));

        assertThat(rows).hasSize(1);
        RawContactRow row = rows.get(0);
        assertThat(row.firstName()).isEqualTo("Amélie");
        assertThat(row.company()).isEqualTo("Babbage, \"Analytical\" Engines");
        assertThat(row.address()).isEqualTo("15 Rue Lepic, Paris");
    }

    private String write(List<Contact> contacts) {
        return new String(writeBytes(contacts), StandardCharsets.UTF_8).replace("﻿", "");
    }

    private byte[] writeBytes(List<Contact> contacts) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.write(contacts, out);
        return out.toByteArray();
    }

    private static Contact contact(
            String firstName,
            String lastName,
            String email,
            String phone,
            String company,
            String jobTitle,
            String address) {
        return new Contact(
                UUID.randomUUID(), firstName, lastName, email, phone, company, jobTitle, address, NOW, NOW);
    }
}
