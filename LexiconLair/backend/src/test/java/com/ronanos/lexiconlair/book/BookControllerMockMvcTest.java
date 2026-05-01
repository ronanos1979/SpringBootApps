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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import(SpringSecurityConfiguration.class)
class BookControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookRepository bookRepository;

    @MockitoBean
    private AuthorRepository authorRepository;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void listBooksReturnsJsonForAuthenticatedUser() throws Exception {
        Author author = new Author("J.R.R.", "Tolkien");
        when(bookRepository.findAll()).thenReturn(List.of(new Book("The Hobbit", author)));

        mockMvc.perform(get("/api/books").with(user("ronan").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("The Hobbit"))
                .andExpect(jsonPath("$[0].author.displayName").value("J.R.R. Tolkien"));
    }

    @Test
    void getBookReturnsBookResponseDto() throws Exception {
        Author author = new Author("Ursula", "Le Guin");
        when(bookRepository.findById(1L)).thenReturn(Optional.of(new Book("A Wizard of Earthsea", author)));

        mockMvc.perform(get("/api/books/1").with(user("ronan").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("A Wizard of Earthsea"))
                .andExpect(jsonPath("$.author.firstName").value("Ursula"))
                .andExpect(jsonPath("$.author.lastName").value("Le Guin"))
                .andExpect(jsonPath("$.author.displayName").value("Ursula Le Guin"));
    }

    @Test
    void createBookReturns201WhenAuthorExists() throws Exception {
        Author author = new Author("Jane", "Austen");
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/books")
                        .with(user("ronan").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Emma",
                                  "authorId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Emma"))
                .andExpect(jsonPath("$.author.displayName").value("Jane Austen"));

        verify(bookRepository).save(argThat(book ->
                book.getTitle().equals("Emma") && book.getAuthor() == author));
    }

    @Test
    void createBookReturns400WhenAuthorIsMissing() throws Exception {
        when(authorRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/books")
                        .with(user("ronan").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Unknown",
                                  "authorId": 99
                                }
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid author id"))
                .andExpect(jsonPath("$.path").value("/api/books"));
    }

    @Test
    void updateBookReturns404WhenBookIsMissing() throws Exception {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/books/99")
                        .with(user("ronan").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Emma",
                                  "authorId": 1
                                }
                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Book not found"))
                .andExpect(jsonPath("$.path").value("/api/books/99"));
    }

    @Test
    void updateBookReturnsUpdatedBookResponseDto() throws Exception {
        Author originalAuthor = new Author("Old", "Author");
        Author updatedAuthor = new Author("Mary", "Shelley");
        Book existing = new Book("Old Title", originalAuthor);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(authorRepository.findById(2L)).thenReturn(Optional.of(updatedAuthor));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/api/books/1")
                        .with(user("ronan").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Frankenstein",
                                  "authorId": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Frankenstein"))
                .andExpect(jsonPath("$.author.displayName").value("Mary Shelley"));
    }

    @Test
    void deleteBookReturns204() throws Exception {
        mockMvc.perform(delete("/api/books/1").with(user("ronan").roles("USER")))
                .andExpect(status().isNoContent());

        verify(bookRepository).deleteById(1L);
    }
}
