package com.contactmanager.application.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.contactmanager.application.mapper.ContactWebMapperImpl;
import com.contactmanager.application.security.CookieBearerTokenResolver;
import com.contactmanager.domain.exception.ContactNotFoundException;
import com.contactmanager.domain.exception.DuplicateEmailException;
import com.contactmanager.domain.model.Contact;
import com.contactmanager.domain.model.ContactSortField;
import com.contactmanager.domain.model.ImportReport;
import com.contactmanager.domain.model.PageQuery;
import com.contactmanager.domain.model.PageResult;
import com.contactmanager.domain.model.SortDirection;
import com.contactmanager.domain.port.in.ContactService;
import com.contactmanager.infrastructure.config.SecurityConfig;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ContactController.class)
@Import({
    SecurityConfig.class,
    ApiExceptionHandler.class,
    ContactWebMapperImpl.class,
    CookieBearerTokenResolver.class
})
class ContactControllerTest {

    private static final Instant CREATED_AT = Instant.parse("2026-03-01T09:00:00Z");
    private static final UUID ADA_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContactService contacts;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Nested
    class AccessControl {

        @Test
        void refusesAnonymousRequest() throws Exception {
            mockMvc.perform(get("/api/contacts")).andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class ListContacts {

        @Test
        void returnsPageWithMetadata() throws Exception {
            given(contacts.list(any(), any())).willReturn(new PageResult<>(List.of(ada()), 0, 20, 1));

            mockMvc.perform(get("/api/contacts").with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(ADA_ID.toString()))
                    .andExpect(jsonPath("$.content[0].firstName").value("Ada"))
                    .andExpect(jsonPath("$.content[0].email").value("ada@example.com"))
                    .andExpect(jsonPath("$.content[0].fullName").value("Ada Lovelace"))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(20))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1));
        }

        @Test
        void passesQueryParameters() throws Exception {
            given(contacts.list(any(), any())).willReturn(new PageResult<>(List.of(), 2, 5, 0));

            mockMvc.perform(get("/api/contacts")
                            .param("search", "ada")
                            .param("page", "2")
                            .param("size", "5")
                            .param("sort", "firstName,desc")
                            .with(jwt()))
                    .andExpect(status().isOk());

            ArgumentCaptor<String> search = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<PageQuery> pageQuery = ArgumentCaptor.forClass(PageQuery.class);
            then(contacts).should().list(search.capture(), pageQuery.capture());

            assertThat(search.getValue()).isEqualTo("ada");
            assertThat(pageQuery.getValue().page()).isEqualTo(2);
            assertThat(pageQuery.getValue().size()).isEqualTo(5);
            assertThat(pageQuery.getValue().sort().field()).isEqualTo(ContactSortField.FIRST_NAME);
            assertThat(pageQuery.getValue().sort().direction()).isEqualTo(SortDirection.DESC);
        }

        @Test
        void rejectsUnknownSortField() throws Exception {
            mockMvc.perform(get("/api/contacts").param("sort", "passwordHash,asc").with(jwt()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("passwordHash")));
        }
    }

    @Nested
    class GetContact {

        @Test
        void answers404ForUnknownContact() throws Exception {
            given(contacts.getById(any())).willThrow(new ContactNotFoundException(ADA_ID));

            mockMvc.perform(get("/api/contacts/{id}", ADA_ID).with(jwt()))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.title").value("Contact not found"));
        }
    }

    @Nested
    class CreateContact {

        @Test
        void createsContact() throws Exception {
            given(contacts.create(any())).willReturn(ada());

            mockMvc.perform(post("/api/contacts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(
                                    """
                                    {"firstName":"Ada","lastName":"Lovelace","email":"ada@example.com",
                                     "company":"Analytical Engines"}
                                    """)
                            .with(jwt())
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/contacts/" + ADA_ID))
                    .andExpect(jsonPath("$.id").value(ADA_ID.toString()));

            ArgumentCaptor<Contact> submitted = ArgumentCaptor.forClass(Contact.class);
            then(contacts).should().create(submitted.capture());
            assertThat(submitted.getValue().firstName()).isEqualTo("Ada");
            assertThat(submitted.getValue().email()).isEqualTo("ada@example.com");
            assertThat(submitted.getValue().id()).as("the client does not choose the identity").isNull();
        }

        @Test
        void answers409ForDuplicateEmail() throws Exception {
            given(contacts.create(any())).willThrow(new DuplicateEmailException("ada@example.com"));

            mockMvc.perform(post("/api/contacts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"firstName":"Ada","lastName":"Lovelace","email":"ada@example.com"}""")
                            .with(jwt())
                            .with(csrf()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Email already used"));
        }
    }

    @Nested
    class Csv {

        @Test
        void importsUploadedFile() throws Exception {
            given(contacts.importFromCsv(any()))
                    .willReturn(new ImportReport(2, List.of(new ImportReport.ImportError(4, "email is required"))));

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "contacts.csv",
                    "text/csv",
                    "firstName,lastName,email\nAda,Lovelace,ada@example.com\n".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(multipart("/api/contacts/import").file(file).with(jwt()).with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.imported").value(2))
                    .andExpect(jsonPath("$.skipped").value(1))
                    .andExpect(jsonPath("$.total").value(3))
                    .andExpect(jsonPath("$.errors[0].lineNumber").value(4))
                    .andExpect(jsonPath("$.errors[0].message").value("email is required"));
        }

        @Test
        void exportsCsvAttachment() throws Exception {
            doAnswer(invocation -> {
                        OutputStream out = invocation.getArgument(2);
                        out.write("firstName,lastName\nAda,Lovelace\n".getBytes(StandardCharsets.UTF_8));
                        return null;
                    })
                    .when(contacts)
                    .exportToCsv(any(), any(), any());

            mockMvc.perform(get("/api/contacts/export").param("search", "ada").with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(header().string(
                                    "Content-Disposition",
                                    org.hamcrest.Matchers.containsString("attachment; filename=\"contacts.csv\"")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Ada,Lovelace")));
        }
    }

    private static Contact ada() {
        return new Contact(
                ADA_ID,
                "Ada",
                "Lovelace",
                "ada@example.com",
                null,
                "Analytical Engines",
                null,
                null,
                CREATED_AT,
                CREATED_AT);
    }
}
