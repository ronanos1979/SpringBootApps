package com.ronanos.lexiconlair.book;

import com.ronanos.lexiconlair.author.domain.Author;
import com.ronanos.lexiconlair.author.persistence.AuthorRepository;
import com.ronanos.lexiconlair.book.domain.Book;
import com.ronanos.lexiconlair.book.persistence.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class BookRepositoryDataJpaTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Test
    void persistsBookWithAuthorRelationship() {
        Author author = authorRepository.saveAndFlush(new Author("Jane", "Austen"));
        Book book = bookRepository.saveAndFlush(new Book("Emma", author));

        Book actual = bookRepository.findById(book.getId()).orElseThrow();

        assertEquals("Emma", actual.getTitle());
        assertEquals(author.getId(), actual.getAuthor().getId());
        assertEquals("Jane Austen", actual.getAuthor().getDisplayName());
    }

    @Test
    void generatedIdsAreAssigned() {
        Author author = authorRepository.saveAndFlush(new Author("Mary", "Shelley"));
        Book book = bookRepository.saveAndFlush(new Book("Frankenstein", author));

        assertTrue(book.getId() > 0);
    }
}
