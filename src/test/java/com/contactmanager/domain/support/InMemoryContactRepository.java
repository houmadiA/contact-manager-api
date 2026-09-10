package com.contactmanager.domain.support;

import com.contactmanager.domain.model.Contact;
import com.contactmanager.domain.model.PageQuery;
import com.contactmanager.domain.model.PageResult;
import com.contactmanager.domain.model.SortDirection;
import com.contactmanager.domain.model.SortSpec;
import com.contactmanager.domain.port.out.ContactRepository;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryContactRepository implements ContactRepository {

    private final Map<UUID, Contact> contacts = new LinkedHashMap<>();

    @Override
    public Contact save(Contact contact) {
        contacts.put(contact.id(), contact);
        return contact;
    }

    @Override
    public Optional<Contact> findById(UUID id) {
        return Optional.ofNullable(contacts.get(id));
    }

    @Override
    public Optional<Contact> findByEmail(String email) {
        return contacts.values().stream()
                .filter(contact -> contact.email().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public boolean deleteById(UUID id) {
        return contacts.remove(id) != null;
    }

    @Override
    public PageResult<Contact> search(String search, PageQuery pageQuery) {
        List<Contact> matches = findAll(search, pageQuery.sort());
        int from = Math.min(pageQuery.offset(), matches.size());
        int to = Math.min(from + pageQuery.size(), matches.size());
        return new PageResult<>(matches.subList(from, to), pageQuery.page(), pageQuery.size(), matches.size());
    }

    @Override
    public List<Contact> findAll(String search, SortSpec sort) {
        return contacts.values().stream()
                .filter(contact -> matches(contact, search))
                .sorted(comparator(sort))
                .toList();
    }

    private static boolean matches(Contact contact, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String needle = search.trim().toLowerCase(Locale.ROOT);
        return containsIgnoreCase(contact.firstName(), needle)
                || containsIgnoreCase(contact.lastName(), needle)
                || containsIgnoreCase(contact.email(), needle)
                || containsIgnoreCase(contact.company(), needle);
    }

    private static boolean containsIgnoreCase(String haystack, String lowercaseNeedle) {
        return haystack != null && haystack.toLowerCase(Locale.ROOT).contains(lowercaseNeedle);
    }

    private static Comparator<Contact> comparator(SortSpec sort) {
        Comparator<Contact> comparator =
                switch (sort.field()) {
                    case FIRST_NAME -> Comparator.comparing(Contact::firstName, String.CASE_INSENSITIVE_ORDER);
                    case LAST_NAME -> Comparator.comparing(Contact::lastName, String.CASE_INSENSITIVE_ORDER);
                    case EMAIL -> Comparator.comparing(Contact::email);
                    case COMPANY -> Comparator.comparing(
                            Contact::company, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                    case CREATED_AT -> Comparator.comparing(Contact::createdAt);
                    case UPDATED_AT -> Comparator.comparing(Contact::updatedAt);
                };
        return sort.direction() == SortDirection.DESC ? comparator.reversed() : comparator;
    }

    public int size() {
        return contacts.size();
    }
}
