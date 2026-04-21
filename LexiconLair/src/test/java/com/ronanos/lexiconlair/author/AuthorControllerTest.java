package com.ronanos.lexiconlair.author;

import com.ronanos.lexiconlair.author.domain.Author;
import com.ronanos.lexiconlair.author.persistence.AuthorRepository;
import com.ronanos.lexiconlair.author.web.AuthorController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorControllerTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorController controller;

    @Test
    void listAllAuthorsAddsRepositoryResultsToModel() {
        ModelMap model = new ModelMap();
        List<Author> authors = List.of(new Author("Ronan", "OSullivan"), new Author("Second", "Author"));
        when(authorRepository.findAll()).thenReturn(authors);

        String viewName = controller.listAllAuthors(model);

        assertEquals("author/listAuthors", viewName);
        assertSame(authors, model.get("authors"));
    }

    @Test
    void showCreateAuthorFormAddsBlankAuthor() {
        ModelMap model = new ModelMap();

        String viewName = controller.showCreateAuthorForm(model);

        assertEquals("author/addAuthor", viewName);
        assertEquals(Author.class, model.get("author").getClass());
    }

    @Test
    void createAuthorReturnsFormViewWhenValidationFails() {
        Author author = new Author("Ronan", "OSullivan");
        BindingResult result = new BeanPropertyBindingResult(author, "author");
        result.reject("invalid");

        String viewName = controller.createAuthor(author, result);

        assertEquals("author/addAuthor", viewName);
        verify(authorRepository, never()).save(author);
    }

    @Test
    void createAuthorSavesAndRedirectsWhenValidationPasses() {
        Author author = new Author("Ronan", "OSullivan");
        BindingResult result = new BeanPropertyBindingResult(author, "author");

        String viewName = controller.createAuthor(author, result);

        assertEquals("redirect:list-authors", viewName);
        verify(authorRepository).save(author);
    }

    @Test
    void deleteAuthorDeletesByIdAndRedirects() {
        String viewName = controller.deleteAuthor(9L);

        assertEquals("redirect:list-authors", viewName);
        verify(authorRepository).deleteById(9L);
    }

    @Test
    void showUpdateAuthorFormLoadsExistingAuthor() {
        ModelMap model = new ModelMap();
        Author author = new Author("Ronan", "OSullivan");
        when(authorRepository.findById(12L)).thenReturn(Optional.of(author));

        String viewName = controller.showUpdateAuthorForm(12L, model);

        assertEquals("author/addAuthor", viewName);
        assertSame(author, model.get("author"));
    }

    @Test
    void showUpdateAuthorFormThrowsWhenAuthorIsMissing() {
        when(authorRepository.findById(12L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> controller.showUpdateAuthorForm(12L, new ModelMap()));
    }

    @Test
    void updateAuthorReturnsFormViewWhenValidationFails() {
        Author author = new Author("Ronan", "OSullivan");
        BindingResult result = new BeanPropertyBindingResult(author, "author");
        result.reject("invalid");

        String viewName = controller.updateAuthor(author, result);

        assertEquals("author/addAuthor", viewName);
        verify(authorRepository, never()).save(author);
    }

    @Test
    void updateAuthorSavesAndRedirectsWhenValidationPasses() {
        Author author = new Author("Ronan", "OSullivan");
        BindingResult result = new BeanPropertyBindingResult(author, "author");

        String viewName = controller.updateAuthor(author, result);

        assertEquals("redirect:list-authors", viewName);
        verify(authorRepository).save(author);
    }
}
