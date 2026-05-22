package com.ronanos.lexiconlair.book;

import com.ronanos.lexiconlair.author.domain.Author;
import com.ronanos.lexiconlair.author.persistence.AuthorRepository;
import com.ronanos.lexiconlair.book.domain.Book;
import com.ronanos.lexiconlair.book.persistence.BookRepository;
import com.ronanos.lexiconlair.book.web.BookController;
import com.ronanos.lexiconlair.security.SpringSecurityConfiguration;
import com.ronanos.lexiconlair.user.domain.User;
import com.ronanos.lexiconlair.user.persistence.UserRepository;
import com.ronanos.lexiconlair.userbook.persistence.UserBookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
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
    private UserRepository userRepository;

    @MockitoBean
    private UserBookRepository userBookRepository;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private User currentUser() {
        User u = new User();
        u.setId(1L);
        return u;
    }

    private Book savedBook(String title, Author author, Long id) {
        Book b = new Book(title, author);
        ReflectionTestUtils.setField(b, "id", id);
        return b;
    }

    @Test
    void listBooksReturnsJsonForAuthenticatedUser() throws Exception {
        Author author = new Author("J.R.R.", "Tolkien");
        when(bookRepository.findAll()).thenReturn(List.of(new Book("The Hobbit", author)));

        mockMvc.perform(get("/api/books").with(user("ronan").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("The Hobbit"))
                .andExpect(jsonPath("$[0].author.displayName").value("J.R.R. Tolkien"));
    }

    @Test
    void listBooksWithMineParamFiltersToCurrentUser() throws Exception {
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser()));
        Author author = new Author("Jane", "Austen");
        when(bookRepository.findByCreatedByOrderByTitleAsc(1L)).thenReturn(List.of(new Book("Emma", author)));
        when(userBookRepository.findBookIdsByUserId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/books?mine=true").with(user("ronan").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Emma"));
    }

    @Test
    void listBooksWithMineIncludesSavedBooks() throws Exception {
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser()));
        Author author = new Author("Leo", "Tolstoy");
        Book saved = savedBook("War and Peace", author, 7L);
        when(bookRepository.findByCreatedByOrderByTitleAsc(1L)).thenReturn(List.of());
        when(userBookRepository.findBookIdsByUserId(1L)).thenReturn(List.of(7L));
        when(bookRepository.findAllById(List.of(7L))).thenReturn(List.of(saved));

        mockMvc.perform(get("/api/books?mine=true").with(user("ronan").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("War and Peace"));
    }

    @Test
    void regularUsersCannotListAllBooks() throws Exception {
        mockMvc.perform(get("/api/books").with(user("ronan").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void searchBooksReturnsMatchingResults() throws Exception {
        Author author = new Author("J.R.R.", "Tolkien");
        when(bookRepository.findTop10ByTitleContainingIgnoreCaseOrderByTitleAsc("hob"))
                .thenReturn(List.of(new Book("The Hobbit", author)));

        mockMvc.perform(get("/api/books/search?q=hob").with(user("ronan").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("The Hobbit"));
    }

    @Test
    void searchBooksReturnsEmptyForBlankQuery() throws Exception {
        mockMvc.perform(get("/api/books/search?q=").with(user("ronan").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getBookReturnsBookResponseDto() throws Exception {
        Author author = new Author("Ursula", "Le Guin");
        when(bookRepository.findById(1L)).thenReturn(Optional.of(new Book("A Wizard of Earthsea", author)));

        mockMvc.perform(get("/api/books/1").with(user("ronan").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("A Wizard of Earthsea"))
                .andExpect(jsonPath("$.author.displayName").value("Ursula Le Guin"));
    }

    @Test
    void createBookReturns201WhenAuthorExists() throws Exception {
        Author author = new Author("Jane", "Austen");
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/books")
                        .with(user("ronan").roles("ADMIN"))
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
    void createBookReturns409WhenTitleAlreadyExists() throws Exception {
        Author author = new Author("Jane", "Austen");
        when(bookRepository.findByTitleIgnoreCase("Emma")).thenReturn(Optional.of(new Book("Emma", author)));

        mockMvc.perform(post("/api/books")
                        .with(user("ronan").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Emma",
                                  "authorId": 1
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A book with this title already exists"));
    }

    @Test
    void createBookSetsCreatedAtAndCreatedBy() throws Exception {
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser()));
        Author author = new Author("Jane", "Austen");
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/books")
                        .with(user("ronan").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Emma",
                                  "authorId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.createdBy").value(1));

        verify(bookRepository).save(argThat(book ->
                book.getCreatedAt() != null && Long.valueOf(1L).equals(book.getCreatedBy())));
    }

    @Test
    void saveToCollectionReturns200AndCreatesUserBook() throws Exception {
        Author author = new Author("Jane", "Austen");
        Book book = savedBook("Emma", author, 5L);
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser()));
        when(bookRepository.findById(5L)).thenReturn(Optional.of(book));
        when(userBookRepository.existsByUserIdAndBookId(1L, 5L)).thenReturn(false);

        mockMvc.perform(post("/api/books/5/save").with(user("ronan").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Emma"));

        verify(userBookRepository).save(argThat(ub ->
                Long.valueOf(1L).equals(ub.getUserId()) && Long.valueOf(5L).equals(ub.getBookId())));
    }

    @Test
    void saveToCollectionIsIdempotentWhenAlreadySaved() throws Exception {
        Author author = new Author("Jane", "Austen");
        Book book = savedBook("Emma", author, 5L);
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser()));
        when(bookRepository.findById(5L)).thenReturn(Optional.of(book));
        when(userBookRepository.existsByUserIdAndBookId(1L, 5L)).thenReturn(true);

        mockMvc.perform(post("/api/books/5/save").with(user("ronan").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void saveToCollectionReturns404WhenBookMissing() throws Exception {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/books/99/save").with(user("ronan").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBookReturns400WhenAuthorIsMissing() throws Exception {
        when(authorRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/books")
                        .with(user("ronan").roles("ADMIN"))
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
                        .with(user("ronan").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Emma",
                                  "authorId": 1
                                }
                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBookReturns409WhenTitleAlreadyTakenByAnotherBook() throws Exception {
        Author author = new Author("Jane", "Austen");
        Book existing = savedBook("Old Title", author, 1L);
        Book conflict = savedBook("Emma", author, 2L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookRepository.findByTitleIgnoreCase("Emma")).thenReturn(Optional.of(conflict));

        mockMvc.perform(put("/api/books/1")
                        .with(user("ronan").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Emma",
                                  "authorId": 1
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A book with this title already exists"));
    }

    @Test
    void updateBookSetsUpdatedAtAndUpdatedBy() throws Exception {
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser()));
        Author originalAuthor = new Author("Old", "Author");
        Author updatedAuthor = new Author("Mary", "Shelley");
        Book existing = new Book("Old Title", originalAuthor);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(authorRepository.findById(2L)).thenReturn(Optional.of(updatedAuthor));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/api/books/1")
                        .with(user("ronan").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Frankenstein",
                                  "authorId": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Frankenstein"))
                .andExpect(jsonPath("$.updatedAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedBy").value(1));

        verify(bookRepository).save(argThat(book ->
                book.getUpdatedAt() != null && Long.valueOf(1L).equals(book.getUpdatedBy())));
    }

    @Test
    void deleteBookReturns204() throws Exception {
        mockMvc.perform(delete("/api/books/1").with(user("ronan").roles("ADMIN")))
                .andExpect(status().isNoContent());

        verify(bookRepository).deleteById(1L);
    }
}
