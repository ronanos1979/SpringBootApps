package com.ronanos.lexiconlair.book;

import com.ronanos.lexiconlair.author.domain.Author;
import com.ronanos.lexiconlair.author.persistence.AuthorRepository;
import com.ronanos.lexiconlair.book.domain.Book;
import com.ronanos.lexiconlair.book.persistence.BookRepository;
import com.ronanos.lexiconlair.book.web.BookController;
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

@WebMvcTest(BookController.class)
@Import(SpringSecurityConfiguration.class)
class BookControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookRepository bookRepository;

    @MockitoBean
    private AuthorRepository authorRepository;

    @Test
    void listBooksReturnsRepositoryResultsForAuthenticatedUser() throws Exception {
        List<Book> books = List.of(new Book(), new Book());
        when(bookRepository.findAll()).thenReturn(books);

        mockMvc.perform(get("/list-books").with(user("ronan").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("book/listBooks"))
                .andExpect(model().attribute("books", books));
    }

    @Test
    void addBookGetReturnsFormViewWithAuthors() throws Exception {
        List<Author> authors = List.of(new Author("Ronan", "OSullivan"));
        when(authorRepository.findAll()).thenReturn(authors);

        mockMvc.perform(get("/add-book").with(user("ronan").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("book/addBook"))
                .andExpect(model().attributeExists("bookForm"))
                .andExpect(model().attribute("authors", authors));
    }

    @Test
    void addBookPostRedirectsWhenPayloadIsValid() throws Exception {
        Author author = new Author("Ronan", "OSullivan");
        when(authorRepository.findById(7L)).thenReturn(Optional.of(author));

        mockMvc.perform(post("/add-book")
                        .with(csrf())
                        .with(user("ronan").roles("USER"))
                        .param("title", "Lexicon Lair")
                        .param("authorId", "7"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("list-books"));

        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void addBookPostReturnsFormWhenPayloadIsInvalid() throws Exception {
        List<Author> authors = List.of(new Author("Ronan", "OSullivan"));
        when(authorRepository.findAll()).thenReturn(authors);

        mockMvc.perform(post("/add-book")
                        .with(csrf())
                        .with(user("ronan").roles("USER"))
                        .param("title", "")
                        .param("authorId", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("book/addBook"))
                .andExpect(model().attributeHasFieldErrors("bookForm", "title", "authorId"))
                .andExpect(model().attribute("authors", authors));

        verifyNoInteractions(bookRepository);
    }
}
