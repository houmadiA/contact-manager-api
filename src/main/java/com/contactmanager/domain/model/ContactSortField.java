package com.contactmanager.domain.model;

import java.util.Arrays;
import java.util.Locale;

public enum ContactSortField {
    FIRST_NAME("firstName"),
    LAST_NAME("lastName"),
    EMAIL("email"),
    COMPANY("company"),
    CREATED_AT("createdAt"),
    UPDATED_AT("updatedAt");

    private final String propertyName;

    ContactSortField(String propertyName) {
        this.propertyName = propertyName;
    }


    public static ContactSortField fromProperty(String property) {
        if (property == null || property.isBlank()) {
            return LAST_NAME;
        }
        String candidate = property.trim();
        return Arrays.stream(values())
                .filter(field -> field.propertyName.equalsIgnoreCase(candidate)
                        || field.name().equalsIgnoreCase(candidate.toUpperCase(Locale.ROOT)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown sort field: " + property));
    }
}
