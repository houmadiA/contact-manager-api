package com.contactmanager.infrastructure.persistence;

import com.contactmanager.domain.model.Contact;
import com.contactmanager.domain.model.ContactSortField;
import com.contactmanager.domain.model.PageQuery;
import com.contactmanager.domain.model.PageResult;
import com.contactmanager.domain.model.SortDirection;
import com.contactmanager.domain.model.SortSpec;
import com.contactmanager.domain.port.out.ContactRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
@RequiredArgsConstructor
public class ContactPersistenceAdapter implements ContactRepository {

    private final ContactEntityRepository contacts;
    private final ContactPersistenceMapper mapper;

    @Override
    public Contact save(Contact contact) {
        contacts.save(mapper.toEntity(contact));
        return contact;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Contact> findById(UUID id) {
        return contacts.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Contact> findByEmail(String email) {
        return contacts.findByEmailIgnoreCase(email).map(mapper::toDomain);
    }

    @Override
    public boolean deleteById(UUID id) {
        if (!contacts.existsById(id)) {
            return false;
        }
        contacts.deleteById(id);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Contact> search(String search, PageQuery pageQuery) {
        Page<ContactEntity> page = contacts.findAll(
                ContactSpecifications.matching(search),
                PageRequest.of(pageQuery.page(), pageQuery.size(), toSort(pageQuery.sort())));

        return new PageResult<>(
                page.getContent().stream().map(mapper::toDomain).toList(),
                pageQuery.page(),
                pageQuery.size(),
                page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contact> findAll(String search, SortSpec sort) {
        return contacts.findAll(ContactSpecifications.matching(search), toSort(sort)).stream()
                .map(mapper::toDomain)
                .toList();
    }

    private static Sort toSort(SortSpec sortSpec) {
        Sort.Order order = new Sort.Order(
                sortSpec.direction() == SortDirection.DESC ? Sort.Direction.DESC : Sort.Direction.ASC,
                propertyOf(sortSpec.field()));

        return Sort.by(isTextual(sortSpec.field()) ? order.ignoreCase() : order);
    }

    private static String propertyOf(ContactSortField field) {
        return switch (field) {
            case FIRST_NAME -> "firstName";
            case LAST_NAME -> "lastName";
            case EMAIL -> "email";
            case COMPANY -> "company";
            case CREATED_AT -> "createdAt";
            case UPDATED_AT -> "updatedAt";
        };
    }

    private static boolean isTextual(ContactSortField field) {
        return switch (field) {
            case FIRST_NAME, LAST_NAME, EMAIL, COMPANY -> true;
            case CREATED_AT, UPDATED_AT -> false;
        };
    }
}
