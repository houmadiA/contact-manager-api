package com.contactmanager.domain.model;

import java.util.Locale;

public enum SortDirection {
    ASC,
    DESC;


    public static SortDirection parse(String value) {
        if (value == null || value.isBlank()) {
            return ASC;
        }
        return SortDirection.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
