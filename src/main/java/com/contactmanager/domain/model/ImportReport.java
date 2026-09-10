package com.contactmanager.domain.model;

import java.util.List;
import java.util.Objects;

public record ImportReport(int imported, List<ImportError> errors) {

    public ImportReport {
        errors = List.copyOf(Objects.requireNonNull(errors, "errors must not be null"));
        if (imported < 0) {
            throw new IllegalArgumentException("imported must not be negative");
        }
    }

    public int skipped() {
        return errors.size();
    }

    public int total() {
        return imported + skipped();
    }

    public record ImportError(int lineNumber, String message) {
    }
}
