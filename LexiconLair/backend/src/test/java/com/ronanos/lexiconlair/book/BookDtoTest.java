package com.ronanos.lexiconlair.book;

import com.ronanos.lexiconlair.author.domain.Author;
import com.ronanos.lexiconlair.book.domain.Book;
import com.ronanos.lexiconlair.book.dto.BookResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
