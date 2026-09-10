package com.contactmanager.application.controller;

import com.contactmanager.domain.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@RestControllerAdvice
@Slf4j
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ContactNotFoundException.class)
    ProblemDetail handleContactNotFound(ContactNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Contact not found", exception.getMessage());
    }

    @ExceptionHandler(DuplicateEmailException.class)
    ProblemDetail handleDuplicateEmail(DuplicateEmailException exception) {
        ProblemDetail problem = problem(HttpStatus.CONFLICT, "Email already used", exception.getMessage());
        problem.setProperty("errors", Map.of("email", exception.getMessage()));
        return problem;
    }

    @ExceptionHandler(InvalidContactException.class)
    ProblemDetail handleInvalidContact(InvalidContactException exception) {
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Invalid contact", exception.getMessage());
        problem.setProperty("errors", Map.of(exception.field(), exception.getMessage()));
        return problem;
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ProblemDetail handleInvalidCredentials(InvalidCredentialsException exception) {
        return problem(HttpStatus.UNAUTHORIZED, "Authentication failed", exception.getMessage());
    }

    @ExceptionHandler(CsvImportException.class)
    ProblemDetail handleCsvImport(CsvImportException exception) {
        return problem(HttpStatus.BAD_REQUEST, "CSV import failed", exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception exception) {
        log.error("Unhandled exception while serving a request", exception);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", "Something went wrong");
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
