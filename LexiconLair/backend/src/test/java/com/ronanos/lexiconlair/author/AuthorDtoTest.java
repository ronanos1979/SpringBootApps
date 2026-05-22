package com.ronanos.lexiconlair.author;

import com.ronanos.lexiconlair.author.domain.Author;
import com.ronanos.lexiconlair.author.dto.AuthorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @Test
    void authorResponseMapsAuditFields() {
        Author author = new Author("Jane", "Austen");
        LocalDateTime now = LocalDateTime.of(2026, 5, 22, 10, 0);
        author.setCreatedAt(now);
        author.setCreatedBy(7L);
        author.setUpdatedAt(now.plusHours(1));
        author.setUpdatedBy(8L);

        AuthorResponse response = AuthorResponse.from(author);

        assertEquals(now, response.createdAt());
        assertEquals(7L, response.createdBy());
        assertEquals(now.plusHours(1), response.updatedAt());
        assertEquals(8L, response.updatedBy());
    }

    @Test
    void authorResponseHasNullAuditFieldsWhenNotSet() {
        Author author = new Author("Jane", "Austen");

        AuthorResponse response = AuthorResponse.from(author);

        assertNull(response.createdAt());
        assertNull(response.createdBy());
        assertNull(response.updatedAt());
        assertNull(response.updatedBy());
    }
}
