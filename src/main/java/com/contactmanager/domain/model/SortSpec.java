package com.contactmanager.domain.model;

import java.util.Objects;

public record SortSpec(ContactSortField field, SortDirection direction) {

    public SortSpec {
        Objects.requireNonNull(field, "sort field must not be null");
        Objects.requireNonNull(direction, "sort direction must not be null");
    }

    public static SortSpec byDefault() {
        return new SortSpec(ContactSortField.LAST_NAME, SortDirection.ASC);
    }

    public static SortSpec of(String property, String direction) {
        return new SortSpec(ContactSortField.fromProperty(property), SortDirection.parse(direction));
    }
}
