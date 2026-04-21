package com.ronanos.lexiconlair.author;

import com.ronanos.lexiconlair.author.domain.Author;
import com.ronanos.lexiconlair.author.persistence.AuthorRepository;
import com.ronanos.lexiconlair.author.web.AuthorController;
import com.ronanos.lexiconlair.security.SpringSecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AuthorController.class)
@Import(SpringSecurityConfiguration.class)
class AuthorControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthorRepository authorRepository;

    @Test
    void listAuthorsReturnsRepositoryResultsForAuthenticatedUser() throws Exception {
        List<Author> authors = List.of(new Author("Ronan", "OSullivan"));
        when(authorRepository.findAll()).thenReturn(authors);

        mockMvc.perform(get("/list-authors").with(user("ronan").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("author/listAuthors"))
                .andExpect(model().attribute("authors", authors));
    }

    @Test
    void addAuthorGetReturnsFormView() throws Exception {
        mockMvc.perform(get("/add-author").with(user("ronan").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("author/addAuthor"))
                .andExpect(model().attributeExists("author"));
    }

    @Test
    void addAuthorPostRedirectsWhenPayloadIsValid() throws Exception {
        mockMvc.perform(post("/add-author")
                        .with(csrf())
                        .with(user("ronan").roles("USER"))
                        .param("firstName", "Ronan")
                        .param("lastName", "OSullivan"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("list-authors"));

        verify(authorRepository).save(any(Author.class));
    }

    @Test
    void addAuthorPostReturnsFormWhenPayloadIsInvalid() throws Exception {
        mockMvc.perform(post("/add-author")
                        .with(csrf())
                        .with(user("ronan").roles("USER"))
                        .param("firstName", "")
                        .param("lastName", "OSullivan"))
                .andExpect(status().isOk())
                .andExpect(view().name("author/addAuthor"))
                .andExpect(model().attributeHasFieldErrors("author", "firstName"));

        verifyNoInteractions(authorRepository);
    }

    @Test
    void updateAuthorGetLoadsExistingAuthor() throws Exception {
        Author existingAuthor = new Author("Ronan", "OSullivan");
        when(authorRepository.findById(11L)).thenReturn(Optional.of(existingAuthor));

        mockMvc.perform(get("/update-author").with(user("ronan").roles("USER")).param("id", "11"))
                .andExpect(status().isOk())
                .andExpect(view().name("author/addAuthor"))
                .andExpect(model().attribute("author", existingAuthor));
    }

    @Test
    void deleteAuthorRedirectsAfterDeletingEntity() throws Exception {
        mockMvc.perform(post("/delete-author")
                        .with(csrf())
                        .with(user("ronan").roles("USER"))
                        .param("id", "17"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("list-authors"));

        verify(authorRepository).deleteById(17L);
    }
}
