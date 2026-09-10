package com.contactmanager.domain.service;

import com.contactmanager.domain.exception.ContactNotFoundException;
import com.contactmanager.domain.exception.DomainException;
import com.contactmanager.domain.exception.DuplicateEmailException;
import com.contactmanager.domain.model.*;
import com.contactmanager.domain.port.in.ContactService;
import com.contactmanager.domain.port.out.ContactCsvReader;
import com.contactmanager.domain.port.out.ContactCsvWriter;
import com.contactmanager.domain.port.out.ContactRepository;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final ContactCsvReader csvReader;
    private final ContactCsvWriter csvWriter;
    private final Clock clock;

    public ContactServiceImpl(
            ContactRepository contactRepository,
            ContactCsvReader csvReader,
            ContactCsvWriter csvWriter,
            Clock clock) {
        this.contactRepository = Objects.requireNonNull(contactRepository);
        this.csvReader = Objects.requireNonNull(csvReader);
        this.csvWriter = Objects.requireNonNull(csvWriter);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public Contact create(Contact contact) {
        requireEmailAvailable(contact.email(), null);

        Instant now = clock.instant();
        return contactRepository.save(new Contact(
                UUID.randomUUID(),
                contact.firstName(),
                contact.lastName(),
                contact.email(),
                contact.phoneNumber(),
                contact.company(),
                contact.jobTitle(),
                contact.address(),
                now,
                now));
    }

    @Override
    public Contact update(UUID id, Contact contact) {
        Contact existing = getById(id);
        requireEmailAvailable(contact.email(), id);

        return contactRepository.save(existing.withDetailsOf(contact, clock.instant()));
    }

    @Override
    public Contact getById(UUID id) {
        return contactRepository.findById(id).orElseThrow(() -> new ContactNotFoundException(id));
    }

    @Override
    public void delete(UUID id) {
        if (!contactRepository.deleteById(id)) {
            throw new ContactNotFoundException(id);
        }
    }

    @Override
    public PageResult<Contact> list(String search, PageQuery pageQuery) {
        return contactRepository.search(search, pageQuery);
    }

    @Override
    public ImportReport importFromCsv(InputStream csv) {
        List<ImportReport.ImportError> errors = new ArrayList<>();
        int imported = 0;

        for (RawContactRow row : csvReader.read(csv)) {
            try {
                create(toContact(row));
                imported++;
            } catch (DomainException failure) {
                errors.add(new ImportReport.ImportError(row.lineNumber(), failure.getMessage()));
            }
        }

        return new ImportReport(imported, errors);
    }

    @Override
    public void exportToCsv(String search, SortSpec sort, OutputStream target) {
        csvWriter.write(contactRepository.findAll(search, sort), target);
    }

    private static Contact toContact(RawContactRow row) {
        return new Contact(
                null,
                row.firstName(),
                row.lastName(),
                row.email(),
                row.phoneNumber(),
                row.company(),
                row.jobTitle(),
                row.address(),
                null,
                null);
    }

    private void requireEmailAvailable(String email, UUID ownerId) {
        contactRepository
                .findByEmail(email)
                .filter(owner -> !owner.id().equals(ownerId))
                .ifPresent(owner -> {
                    throw new DuplicateEmailException(email);
                });
    }
}
