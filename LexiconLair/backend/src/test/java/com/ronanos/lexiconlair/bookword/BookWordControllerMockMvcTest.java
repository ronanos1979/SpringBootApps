package com.ronanos.lexiconlair.bookword;

import com.ronanos.lexiconlair.author.domain.Author;
import com.ronanos.lexiconlair.book.domain.Book;
import com.ronanos.lexiconlair.book.persistence.BookRepository;
import com.ronanos.lexiconlair.bookword.domain.BookWord;
import com.ronanos.lexiconlair.bookword.persistence.BookWordRepository;
import com.ronanos.lexiconlair.bookword.web.BookWordController;
import com.ronanos.lexiconlair.definition.persistence.DefinitionRepository;
import com.ronanos.lexiconlair.security.SpringSecurityConfiguration;
import com.ronanos.lexiconlair.user.domain.User;
import com.ronanos.lexiconlair.user.persistence.UserRepository;
import com.ronanos.lexiconlair.word.domain.Word;
import com.ronanos.lexiconlair.word.service.WordDefinitionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookWordController.class)
@Import(SpringSecurityConfiguration.class)
class BookWordControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookWordRepository bookWordRepository;

    @MockitoBean
    private BookRepository bookRepository;

    @MockitoBean
    private DefinitionRepository definitionRepository;

    @MockitoBean
    private WordDefinitionService wordDefinitionService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private User currentUser() {
        User u = new User();
        u.setId(1L);
        return u;
    }

    private Book book() {
        Author author = new Author("William", "Shakespeare");
        Book b = new Book("Hamlet", author);
        ReflectionTestUtils.setField(b, "id", 1L);
        return b;
    }

    private Word word() {
        Word w = new Word("ephemeral", "en");
        ReflectionTestUtils.setField(w, "id", 2L);
        return w;
    }

    private BookWord bookWord(Book book, Word word) {
        BookWord bw = new BookWord();
        bw.setBook(book);
        bw.setWord(word);
        bw.setCreatedAt(LocalDateTime.of(2026, 5, 22, 10, 0));
        bw.setCreatedBy(1L);
        ReflectionTestUtils.setField(bw, "id", 5L);
        return bw;
    }

    @Test
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/books/1/words"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listWordsForBookReturnsMyWordsByDefault() throws Exception {
        Book book = book();
        Word word = word();
        BookWord bw = bookWord(book, word);

        when(bookRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser()));
        when(bookWordRepository.findByBook_IdAndCreatedByOrderByCreatedAtDesc(1L, 1L))
                .thenReturn(List.of(bw));
        when(definitionRepository.findByWord_Id(2L)).thenReturn(List.of());

        mockMvc.perform(get("/api/books/1/words").with(user("ronan").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].word.text").value("ephemeral"))
                .andExpect(jsonPath("$[0].word.language").value("en"))
                .andExpect(jsonPath("$[0].createdBy").value(1));
    }

    @Test
    void listWordsForBookWithMineFalseReturnsAllWords() throws Exception {
        Book book = book();
        Word word = word();
        BookWord bw = bookWord(book, word);

        when(bookRepository.existsById(1L)).thenReturn(true);
        when(bookWordRepository.findByBook_IdOrderByCreatedAtDesc(1L)).thenReturn(List.of(bw));
        when(definitionRepository.findByWord_Id(2L)).thenReturn(List.of());

        mockMvc.perform(get("/api/books/1/words?mine=false").with(user("ronan").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].word.text").value("ephemeral"));
    }

    @Test
    void listWordsForBookReturns404WhenBookMissing() throws Exception {
        when(bookRepository.existsById(99L)).thenReturn(false);

        mockMvc.perform(get("/api/books/99/words").with(user("ronan").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void addWordToBookReturns201() throws Exception {
        Book book = book();
        Word word = word();

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser()));
        when(wordDefinitionService.findOrCreateWordWithDefinitions("ephemeral", "en", 1L))
                .thenReturn(word);
        when(bookWordRepository.existsByBook_IdAndWord_Id(1L, 2L)).thenReturn(false);
        when(bookWordRepository.save(any(BookWord.class))).thenAnswer(inv -> {
            BookWord bw = inv.getArgument(0);
            ReflectionTestUtils.setField(bw, "id", 5L);
            return bw;
        });
        when(definitionRepository.findByWord_Id(2L)).thenReturn(List.of());

        mockMvc.perform(post("/api/books/1/words")
                        .with(user("ronan").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "ephemeral",
                                  "language": "en"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.word.text").value("ephemeral"))
                .andExpect(jsonPath("$.bookId").value(1))
                .andExpect(jsonPath("$.createdBy").value(1));

        verify(bookWordRepository).save(argThat(bw ->
                bw.getCreatedAt() != null && Long.valueOf(1L).equals(bw.getCreatedBy())));
    }

    @Test
    void addWordToBookReturns409WhenWordAlreadyPresent() throws Exception {
        Book book = book();
        Word word = word();

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser()));
        when(wordDefinitionService.findOrCreateWordWithDefinitions("ephemeral", "en", 1L))
                .thenReturn(word);
        when(bookWordRepository.existsByBook_IdAndWord_Id(1L, 2L)).thenReturn(true);

        mockMvc.perform(post("/api/books/1/words")
                        .with(user("ronan").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "ephemeral",
                                  "language": "en"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Word already added to this book"));
    }

    @Test
    void addWordToBookReturns404WhenBookMissing() throws Exception {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/books/99/words")
                        .with(user("ronan").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "ephemeral",
                                  "language": "en"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void addWordToBookRejectsBlankText() throws Exception {
        mockMvc.perform(post("/api/books/1/words")
                        .with(user("ronan").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "",
                                  "language": "en"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void removeWordFromBookReturns204() throws Exception {
        Book book = book();
        Word word = word();
        BookWord bw = bookWord(book, word);

        when(bookWordRepository.findById(5L)).thenReturn(Optional.of(bw));

        mockMvc.perform(delete("/api/books/1/words/5").with(user("ronan").roles("ADMIN")))
                .andExpect(status().isNoContent());

        verify(bookWordRepository).deleteById(5L);
    }

    @Test
    void removeWordFromBookReturns404WhenEntryMissing() throws Exception {
        when(bookWordRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/books/1/words/99").with(user("ronan").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeWordFromBookReturns400WhenBookIdMismatch() throws Exception {
        Book otherBook = new Book("Othello", new Author("William", "Shakespeare"));
        ReflectionTestUtils.setField(otherBook, "id", 99L);
        BookWord bw = bookWord(otherBook, word());

        when(bookWordRepository.findById(5L)).thenReturn(Optional.of(bw));

        mockMvc.perform(delete("/api/books/1/words/5").with(user("ronan").roles("ADMIN")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bulkAddWordsReturnsAddedAndDuplicates() throws Exception {
        Book book = book();
        Word wordEph = word();
        Word wordSer = new Word("serendipity", "en");
        ReflectionTestUtils.setField(wordSer, "id", 3L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser()));

        when(wordDefinitionService.findOrCreateWordWithDefinitions("ephemeral", "en", 1L)).thenReturn(wordEph);
        when(wordDefinitionService.findOrCreateWordWithDefinitions("serendipity", "en", 1L)).thenReturn(wordSer);

        when(bookWordRepository.existsByBook_IdAndWord_Id(1L, 2L)).thenReturn(true);
        when(bookWordRepository.existsByBook_IdAndWord_Id(1L, 3L)).thenReturn(false);
        when(bookWordRepository.save(any(BookWord.class))).thenAnswer(inv -> {
            BookWord bw = inv.getArgument(0);
            ReflectionTestUtils.setField(bw, "id", 10L);
            return bw;
        });
        when(definitionRepository.findByWord_Id(3L)).thenReturn(List.of());

        mockMvc.perform(post("/api/books/1/words/bulk")
                        .with(user("ronan").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "words": ["ephemeral", "serendipity"],
                                  "language": "en"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.added").isArray())
                .andExpect(jsonPath("$.added.length()").value(1))
                .andExpect(jsonPath("$.added[0].word.text").value("serendipity"))
                .andExpect(jsonPath("$.duplicates").isArray())
                .andExpect(jsonPath("$.duplicates.length()").value(1))
                .andExpect(jsonPath("$.duplicates[0]").value("ephemeral"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void bulkAddWordsDeduplicatesInputList() throws Exception {
        Book book = book();
        Word wordEph = word();

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser()));
        when(wordDefinitionService.findOrCreateWordWithDefinitions("ephemeral", "en", 1L)).thenReturn(wordEph);
        when(bookWordRepository.existsByBook_IdAndWord_Id(1L, 2L)).thenReturn(false);
        when(bookWordRepository.save(any(BookWord.class))).thenAnswer(inv -> {
            BookWord bw = inv.getArgument(0);
            ReflectionTestUtils.setField(bw, "id", 10L);
            return bw;
        });
        when(definitionRepository.findByWord_Id(2L)).thenReturn(List.of());

        mockMvc.perform(post("/api/books/1/words/bulk")
                        .with(user("ronan").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "words": ["ephemeral", "ephemeral", "EPHEMERAL"],
                                  "language": "en"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.added.length()").value(1));
    }

    @Test
    void bulkAddWordsReturns404WhenBookMissing() throws Exception {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/books/99/words/bulk")
                        .with(user("ronan").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "words": ["ephemeral"],
                                  "language": "en"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void bulkAddWordsReturns400WhenWordsListIsEmpty() throws Exception {
        mockMvc.perform(post("/api/books/1/words/bulk")
                        .with(user("ronan").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "words": [],
                                  "language": "en"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bulkAddWordsSetsCreatedAtAndCreatedByOnEachEntry() throws Exception {
        Book book = book();
        Word wordEph = word();

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser()));
        when(wordDefinitionService.findOrCreateWordWithDefinitions("ephemeral", "en", 1L)).thenReturn(wordEph);
        when(bookWordRepository.existsByBook_IdAndWord_Id(1L, 2L)).thenReturn(false);
        when(bookWordRepository.save(any(BookWord.class))).thenAnswer(inv -> {
            BookWord bw = inv.getArgument(0);
            ReflectionTestUtils.setField(bw, "id", 10L);
            return bw;
        });
        when(definitionRepository.findByWord_Id(2L)).thenReturn(List.of());

        mockMvc.perform(post("/api/books/1/words/bulk")
                        .with(user("ronan").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "words": ["ephemeral"],
                                  "language": "en"
                                }
                                """))
                .andExpect(status().isOk());

        verify(bookWordRepository).save(argThat(bw ->
                bw.getCreatedAt() != null && Long.valueOf(1L).equals(bw.getCreatedBy())));
    }
}
