package com.contactmanager.domain.model;

import java.util.Objects;

public record PageQuery(int page, int size, SortSpec sort) {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 200;

    public PageQuery {
        if (page < 0) {
            throw new IllegalArgumentException("page must not be negative");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new IllegalArgumentException("size must be between 1 and " + MAX_SIZE);
        }
        Objects.requireNonNull(sort, "sort must not be null");
    }

    public static PageQuery firstPage(int size) {
        return new PageQuery(0, size, SortSpec.byDefault());
    }

    public int offset() {
        return page * size;
    }
}
