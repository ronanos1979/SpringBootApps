package com.ronanos.lexiconlair.book;

import com.ronanos.lexiconlair.book.web.BookForm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookFormTest {

    @Test
    void gettersSettersAndToStringExposeFormState() {
        BookForm bookForm = new BookForm();
        bookForm.setTitle("Lexicon Lair");
        bookForm.setAuthorId(9L);

        assertEquals("Lexicon Lair", bookForm.getTitle());
        assertEquals(9L, bookForm.getAuthorId());
        assertTrue(bookForm.toString().contains("Lexicon Lair"));
        assertTrue(bookForm.toString().contains("authorId=9"));
    }
}
