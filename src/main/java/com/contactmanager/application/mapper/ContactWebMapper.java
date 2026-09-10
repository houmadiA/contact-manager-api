package com.contactmanager.application.mapper;

import com.contactmanager.application.dto.*;
import com.contactmanager.domain.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mapper
public interface ContactWebMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Contact toContact(ContactRequest request);

    @Mapping(target = "fullName", expression = "java(contact.fullName())")
    ContactResponse toResponse(Contact contact);

    List<ContactResponse> toResponses(List<Contact> contacts);

    default PageResponse<ContactResponse> toResponse(PageResult<Contact> page) {
        return new PageResponse<>(
                toResponses(page.content()), page.page(), page.size(), page.totalElements(), page.totalPages());
    }

    @Mapping(target = "skipped", expression = "java(report.skipped())")
    @Mapping(target = "total", expression = "java(report.total())")
    ImportReportResponse toResponse(ImportReport report);

    ImportReportResponse.ImportErrorResponse toResponse(ImportReport.ImportError error);

    UserResponse toResponse(User user);

    default String toId(UUID id) {
        return id == null ? null : id.toString();
    }

    default List<String> toRoleNames(Set<Role> roles) {
        return roles == null ? List.of() : roles.stream().map(Role::name).sorted().toList();
    }
}
