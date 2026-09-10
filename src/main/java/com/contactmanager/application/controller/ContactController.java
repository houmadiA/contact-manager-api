package com.contactmanager.application.controller;

import com.contactmanager.application.dto.ContactRequest;
import com.contactmanager.application.dto.ContactResponse;
import com.contactmanager.application.dto.ImportReportResponse;
import com.contactmanager.application.dto.PageResponse;
import com.contactmanager.application.mapper.ContactWebMapper;
import com.contactmanager.domain.model.Contact;
import com.contactmanager.domain.model.PageQuery;
import com.contactmanager.domain.model.SortSpec;
import com.contactmanager.domain.port.in.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/contacts")
@Tag(name = "Contacts", description = "Manage the address book")
@RequiredArgsConstructor
class ContactController {

    private final ContactService contacts;
    private final ContactWebMapper mapper;

    @GetMapping
    @Operation(summary = "List contacts, filtered, sorted and paged")
    PageResponse<ContactResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "lastName,asc") String sort) {

        return mapper.toResponse(contacts.list(search, new PageQuery(page, size, parseSort(sort))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Read one contact")
    ContactResponse getById(@PathVariable UUID id) {
        return mapper.toResponse(contacts.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create a contact")
    ResponseEntity<ContactResponse> create(@RequestBody ContactRequest request) {
        Contact created = contacts.create(mapper.toContact(request));

        return ResponseEntity.created(URI.create("/api/contacts/" + created.id()))
                .body(mapper.toResponse(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace a contact")
    ContactResponse update(@PathVariable UUID id, @RequestBody ContactRequest request) {
        return mapper.toResponse(contacts.update(id, mapper.toContact(request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a contact")
    ResponseEntity<Void> delete(@PathVariable UUID id) {
        contacts.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import contacts from a CSV file, reporting rejected rows")
    ImportReportResponse importCsv(@RequestPart("file") MultipartFile file) {
        try {
            return mapper.toResponse(contacts.importFromCsv(file.getInputStream()));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the uploaded file", e);
        }
    }

    @GetMapping(path = "/export", produces = "text/csv")
    @Operation(summary = "Export the filtered contacts as CSV")
    ResponseEntity<StreamingResponseBody> exportCsv(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "lastName,asc") String sort) {

        SortSpec sortSpec = parseSort(sort);

        // Streamed rather than buffered: the response is written as rows are read.
        StreamingResponseBody body = outputStream -> contacts.exportToCsv(search, sortSpec, outputStream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"contacts.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(body);
    }

    private static SortSpec parseSort(String sort) {
        String[] parts = sort.split(",", 2);
        return SortSpec.of(parts[0], parts.length > 1 ? parts[1] : "asc");
    }
}
