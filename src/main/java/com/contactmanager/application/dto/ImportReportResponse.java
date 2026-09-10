package com.contactmanager.application.dto;

import java.util.List;

public record ImportReportResponse(int imported, int skipped, int total, List<ImportErrorResponse> errors) {

    public record ImportErrorResponse(int lineNumber, String message) {
    }
}
