package com.contactmanager.domain.port.in;

import com.contactmanager.domain.model.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public interface ContactService {

    Contact create(Contact contact);

    Contact update(UUID id, Contact contact);

    Contact getById(UUID id);

    void delete(UUID id);

    PageResult<Contact> list(String search, PageQuery pageQuery);

    ImportReport importFromCsv(InputStream csv);

    void exportToCsv(String search, SortSpec sort, OutputStream target);
}
