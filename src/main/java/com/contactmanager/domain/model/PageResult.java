package com.contactmanager.domain.model;

import java.util.List;
import java.util.Objects;

public record PageResult<T>(List<T> content, int page, int size, long totalElements) {

    public PageResult {
        content = List.copyOf(Objects.requireNonNull(content, "content must not be null"));
        if (page < 0) {
            throw new IllegalArgumentException("page must not be negative");
        }
        if (size < 1) {
            throw new IllegalArgumentException("size must be positive");
        }
        if (totalElements < 0) {
            throw new IllegalArgumentException("totalElements must not be negative");
        }
    }

    public int totalPages() {
        return (int) Math.ceil((double) totalElements / size);
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }
}
