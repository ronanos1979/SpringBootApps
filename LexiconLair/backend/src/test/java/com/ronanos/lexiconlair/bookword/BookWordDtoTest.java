package com.ronanos.lexiconlair.bookword;

import com.ronanos.lexiconlair.author.domain.Author;
import com.ronanos.lexiconlair.book.domain.Book;
import com.ronanos.lexiconlair.bookword.domain.BookWord;
import com.ronanos.lexiconlair.bookword.dto.BookWordResponse;
import com.ronanos.lexiconlair.bookword.dto.DefinitionSummary;
import com.ronanos.lexiconlair.bookword.dto.WordSearchResult;
import com.ronanos.lexiconlair.definition.domain.Definition;
import com.ronanos.lexiconlair.word.domain.Word;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookWordDtoTest {

    private Author author() {
        Author a = new Author("William", "Shakespeare");
        ReflectionTestUtils.setField(a, "id", 10L);
        return a;
    }

    private Book book(Author author) {
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
        bw.setCreatedBy(5L);
        ReflectionTestUtils.setField(bw, "id", 7L);
        return bw;
    }

    private Definition definition() {
        Definition d = new Definition();
        ReflectionTestUtils.setField(d, "id", 3L);
        d.setWord(word());
        d.setDefinitionText("Lasting for a very short time.");
        d.setPartOfSpeech("adjective");
        d.setExample("An ephemeral moment.");
        d.setSourceApi("dictionary-api");
        d.setCachedAt(LocalDateTime.of(2026, 5, 1, 8, 0));
        return d;
    }

    // DefinitionSummary tests

    @Test
    void definitionSummaryMapsAllFields() {
        Definition d = definition();

        DefinitionSummary summary = DefinitionSummary.from(d);

        assertEquals(3L, summary.id());
        assertEquals("Lasting for a very short time.", summary.definitionText());
        assertEquals("adjective", summary.partOfSpeech());
        assertEquals("An ephemeral moment.", summary.example());
        assertEquals("dictionary-api", summary.sourceApi());
        assertEquals(LocalDateTime.of(2026, 5, 1, 8, 0), summary.cachedAt());
    }

    @Test
    void definitionSummaryHandlesNullExampleAndCachedAt() {
        Definition d = new Definition();
        ReflectionTestUtils.setField(d, "id", 4L);
        d.setWord(word());
        d.setDefinitionText("Short-lived.");
        d.setPartOfSpeech("adjective");
        d.setSourceApi("dictionary-api");

        DefinitionSummary summary = DefinitionSummary.from(d);

        assertNull(summary.example());
        assertNull(summary.cachedAt());
    }

    // BookWordResponse tests

    @Test
    void bookWordResponseMapsAllFields() {
        Book book = book(author());
        Word word = word();
        BookWord bw = bookWord(book, word);
        Definition d = definition();

        BookWordResponse response = BookWordResponse.from(bw, List.of(d));

        assertEquals(7L, response.id());
        assertEquals(1L, response.bookId());
        assertEquals(2L, response.word().id());
        assertEquals("ephemeral", response.word().text());
        assertEquals("en", response.word().language());
        assertEquals(1, response.definitions().size());
        assertEquals(3L, response.definitions().get(0).id());
        assertEquals("Lasting for a very short time.", response.definitions().get(0).definitionText());
        assertEquals(LocalDateTime.of(2026, 5, 22, 10, 0), response.createdAt());
        assertEquals(5L, response.createdBy());
    }

    @Test
    void bookWordResponseWithEmptyDefinitions() {
        Book book = book(author());
        BookWord bw = bookWord(book, word());

        BookWordResponse response = BookWordResponse.from(bw, List.of());

        assertTrue(response.definitions().isEmpty());
    }

    @Test
    void bookWordResponseWithMultipleDefinitions() {
        Book book = book(author());
        BookWord bw = bookWord(book, word());

        Definition d1 = new Definition();
        ReflectionTestUtils.setField(d1, "id", 10L);
        d1.setWord(word());
        d1.setDefinitionText("First meaning.");
        d1.setPartOfSpeech("noun");
        d1.setSourceApi("api");

        Definition d2 = new Definition();
        ReflectionTestUtils.setField(d2, "id", 11L);
        d2.setWord(word());
        d2.setDefinitionText("Second meaning.");
        d2.setPartOfSpeech("verb");
        d2.setSourceApi("api");

        BookWordResponse response = BookWordResponse.from(bw, List.of(d1, d2));

        assertEquals(2, response.definitions().size());
        assertEquals(10L, response.definitions().get(0).id());
        assertEquals(11L, response.definitions().get(1).id());
    }

    // WordSearchResult tests

    @Test
    void wordSearchResultMapsAllFields() {
        Author author = author();
        Book book = book(author);
        Word word = word();
        BookWord bw = bookWord(book, word);
        Definition d = definition();

        WordSearchResult result = WordSearchResult.from(bw, List.of(d));

        assertEquals(7L, result.bookWordId());
        assertEquals(1L, result.bookId());
        assertEquals("Hamlet", result.bookTitle());
        assertEquals("William Shakespeare", result.authorDisplayName());
        assertEquals(2L, result.word().id());
        assertEquals("ephemeral", result.word().text());
        assertEquals("en", result.word().language());
        assertEquals(1, result.definitions().size());
        assertEquals(3L, result.definitions().get(0).id());
        assertEquals(LocalDateTime.of(2026, 5, 22, 10, 0), result.createdAt());
    }

    @Test
    void wordSearchResultWithEmptyDefinitions() {
        Author author = author();
        Book book = book(author);
        BookWord bw = bookWord(book, word());

        WordSearchResult result = WordSearchResult.from(bw, List.of());

        assertTrue(result.definitions().isEmpty());
    }

    @Test
    void wordSearchResultAuthorDisplayNameFormatted() {
        Author author = new Author("Jane", "Austen");
        ReflectionTestUtils.setField(author, "id", 20L);
        Book book = new Book("Pride and Prejudice", author);
        ReflectionTestUtils.setField(book, "id", 21L);
        Word word = word();
        BookWord bw = bookWord(book, word);

        WordSearchResult result = WordSearchResult.from(bw, List.of());

        assertEquals("Pride and Prejudice", result.bookTitle());
        assertEquals("Jane Austen", result.authorDisplayName());
    }
}
