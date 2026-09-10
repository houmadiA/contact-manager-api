package com.contactmanager.domain.port.out;

import com.contactmanager.domain.model.RawContactRow;

import java.io.InputStream;
import java.util.List;

public interface ContactCsvReader {

    List<RawContactRow> read(InputStream inputStream);
}
