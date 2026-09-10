package com.contactmanager.domain.port.out;

import com.contactmanager.domain.model.Contact;
import java.io.OutputStream;
import java.util.List;

public interface ContactCsvWriter {

    void write(List<Contact> contacts, OutputStream outputStream);
}
