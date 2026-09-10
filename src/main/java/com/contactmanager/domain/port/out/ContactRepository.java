package com.contactmanager.domain.port.out;

import com.contactmanager.domain.model.Contact;
import com.contactmanager.domain.model.PageQuery;
import com.contactmanager.domain.model.PageResult;
import com.contactmanager.domain.model.SortSpec;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContactRepository {

    Contact save(Contact contact);

    Optional<Contact> findById(UUID id);

    Optional<Contact> findByEmail(String email);

    boolean deleteById(UUID id);

    PageResult<Contact> search(String search, PageQuery pageQuery);

    List<Contact> findAll(String search, SortSpec sort);
}
