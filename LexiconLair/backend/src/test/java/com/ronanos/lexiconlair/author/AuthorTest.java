package com.ronanos.lexiconlair.author;

import com.ronanos.lexiconlair.author.domain.Author;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthorTest {

    @Test
    void getDisplayNameCombinesFirstAndLastName() {
        Author author = new Author("Ronan", "OSullivan");

        assertEquals("Ronan OSullivan", author.getDisplayName());
    }
}
