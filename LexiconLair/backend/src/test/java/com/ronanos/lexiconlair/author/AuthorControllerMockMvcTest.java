package com.ronanos.lexiconlair.author;

import com.ronanos.lexiconlair.author.domain.Author;
import com.ronanos.lexiconlair.author.persistence.AuthorRepository;
import com.ronanos.lexiconlair.author.web.AuthorController;
import com.ronanos.lexiconlair.security.SpringSecurityConfiguration;
import com.ronanos.lexiconlair.user.domain.User;
import com.ronanos.lexiconlair.user.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthorController.class)
@Import(SpringSecurityConfiguration.class)
class AuthorControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthorRepository authorRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private User currentUser() {
        User u = new User();
        u.setId(1L);
        return u;
    }

    @Test
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listAuthorsReturnsJsonForAuthenticatedUser() throws Exception {
        when(authorRepository.findAll()).thenReturn(List.of(new Author("Jane", "Austen")));

        mockMvc.perform(get("/api/authors").with(user("ronan").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].firstName").value("Jane"))
                .andExpect(jsonPath("$[0].displayName").value("Jane Austen"));
    }

    @Test
    void getAuthorReturns404WhenMissing() throws Exception {
        when(authorRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/authors/99").with(user("ronan").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Author not found"))
                .andExpect(jsonPath("$.path").value("/api/authors/99"));
    }

    @Test
    void getAuthorReturnsAuthorResponseDto() throws Exception {
        when(authorRepository.findById(1L)).thenReturn(Optional.of(new Author("Octavia", "Butler")));

        mockMvc.perform(get("/api/authors/1").with(user("ronan").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Octavia"))
                .andExpect(jsonPath("$.lastName").value("Butler"))
                .andExpect(jsonPath("$.displayName").value("Octavia Butler"));
    }

    @Test
    void searchAuthorsReturnsMatchingResults() throws Exception {
        when(authorRepository.searchByDisplayName("austen"))
                .thenReturn(List.of(new Author("Jane", "Austen")));

        mockMvc.perform(get("/api/authors/search?q=austen").with(user("ronan").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].displayName").value("Jane Austen"));
    }

    @Test
    void searchAuthorsReturnsEmptyForBlankQuery() throws Exception {
        mockMvc.perform(get("/api/authors/search?q=").with(user("ronan").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void createAuthorReturns201() throws Exception {
        when(authorRepository.save(any(Author.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/authors")
                        .with(user("ronan").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "George",
                                  "lastName": "Orwell"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.displayName").value("George Orwell"));

        verify(authorRepository).save(argThat(author ->
                author.getFirstName().equals("George") && author.getLastName().equals("Orwell")));
    }

    @Test
    void createAuthorSetsCreatedAtAndCreatedBy() throws Exception {
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser()));
        when(authorRepository.save(any(Author.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/authors")
                        .with(user("ronan").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "George",
                                  "lastName": "Orwell"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.createdBy").value(1));

        verify(authorRepository).save(argThat(author ->
                author.getCreatedAt() != null && Long.valueOf(1L).equals(author.getCreatedBy())));
    }

    @Test
    void createAuthorReturns409WhenAuthorAlreadyExists() throws Exception {
        when(authorRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase("George", "Orwell"))
                .thenReturn(Optional.of(new Author("George", "Orwell")));

        mockMvc.perform(post("/api/authors")
                        .with(user("ronan").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "George",
                                  "lastName": "Orwell"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Author already exists"));
    }

    @Test
    void createAuthorRejectsInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/authors")
                        .with(user("ronan").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "",
                                  "lastName": ""
                                }
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/authors"));
    }

    @Test
    void updateAuthorReturnsUpdatedAuthor() throws Exception {
        Author existing = new Author("Old", "Name");
        when(authorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(authorRepository.save(any(Author.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/api/authors/1")
                        .with(user("ronan").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Mary",
                                  "lastName": "Shelley"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Mary Shelley"));
    }

    @Test
    void updateAuthorSetsUpdatedAtAndUpdatedBy() throws Exception {
        Author existing = new Author("Old", "Name");
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser()));
        when(authorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(authorRepository.save(any(Author.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/api/authors/1")
                        .with(user("ronan").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Mary",
                                  "lastName": "Shelley"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedBy").value(1));

        verify(authorRepository).save(argThat(author ->
                author.getUpdatedAt() != null && Long.valueOf(1L).equals(author.getUpdatedBy())));
    }

    @Test
    void deleteAuthorReturns204() throws Exception {
        mockMvc.perform(delete("/api/authors/1").with(user("ronan").roles("ADMIN")))
                .andExpect(status().isNoContent());

        verify(authorRepository).deleteById(1L);
    }
}
