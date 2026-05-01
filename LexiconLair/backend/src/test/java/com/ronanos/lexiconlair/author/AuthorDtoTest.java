package com.ronanos.lexiconlair.author;

import com.ronanos.lexiconlair.author.domain.Author;
import com.ronanos.lexiconlair.author.dto.AuthorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthorDtoTest {

    @Test
    void authorResponseMapsDomainFields() {
        Author author = new Author("Jane", "Austen");
        ReflectionTestUtils.setField(author, "id", 1L);

        AuthorResponse response = AuthorResponse.from(author);

        assertEquals(1L, response.id());
        assertEquals("Jane", response.firstName());
        assertEquals("Austen", response.lastName());
        assertEquals("Jane Austen", response.displayName());
    }
}
