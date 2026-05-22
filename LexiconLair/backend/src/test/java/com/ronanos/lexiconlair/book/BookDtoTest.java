package com.ronanos.lexiconlair.book;

import com.ronanos.lexiconlair.author.domain.Author;
import com.ronanos.lexiconlair.book.domain.Book;
import com.ronanos.lexiconlair.book.dto.BookResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BookDtoTest {

    @Test
    void bookResponseMapsNestedAuthorDto() {
        Author author = new Author("Mary", "Shelley");
        ReflectionTestUtils.setField(author, "id", 2L);
        Book book = new Book("Frankenstein", author);
        ReflectionTestUtils.setField(book, "id", 1L);

        BookResponse response = BookResponse.from(book);

        assertEquals(1L, response.id());
        assertEquals("Frankenstein", response.title());
        assertEquals(2L, response.author().id());
        assertEquals("Mary Shelley", response.author().displayName());
    }

    @Test
    void bookResponseMapsAuditFields() {
        Author author = new Author("Mary", "Shelley");
        Book book = new Book("Frankenstein", author);
        LocalDateTime now = LocalDateTime.of(2026, 5, 22, 10, 0);
        book.setCreatedAt(now);
        book.setCreatedBy(3L);
        book.setUpdatedAt(now.plusDays(1));
        book.setUpdatedBy(4L);

        BookResponse response = BookResponse.from(book);

        assertEquals(now, response.createdAt());
        assertEquals(3L, response.createdBy());
        assertEquals(now.plusDays(1), response.updatedAt());
        assertEquals(4L, response.updatedBy());
    }

    @Test
    void bookResponseHasNullAuditFieldsWhenNotSet() {
        Book book = new Book("Frankenstein", new Author("Mary", "Shelley"));

        BookResponse response = BookResponse.from(book);

        assertNull(response.createdAt());
        assertNull(response.createdBy());
        assertNull(response.updatedAt());
        assertNull(response.updatedBy());
    }
}
