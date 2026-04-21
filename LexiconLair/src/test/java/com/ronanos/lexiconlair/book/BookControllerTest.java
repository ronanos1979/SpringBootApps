package com.ronanos.lexiconlair.book;

import com.ronanos.lexiconlair.author.domain.Author;
import com.ronanos.lexiconlair.author.persistence.AuthorRepository;
import com.ronanos.lexiconlair.book.domain.Book;
import com.ronanos.lexiconlair.book.persistence.BookRepository;
import com.ronanos.lexiconlair.book.web.BookController;
import com.ronanos.lexiconlair.book.web.BookForm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private BookController controller;

    @Test
    void listAllBooksAddsRepositoryResultsToModel() {
        ModelMap model = new ModelMap();
        List<Book> books = List.of(new Book(), new Book());
        when(bookRepository.findAll()).thenReturn(books);

        String viewName = controller.listAllBooks(model);

        assertEquals("book/listBooks", viewName);
        assertSame(books, model.get("books"));
    }

    @Test
    void showCreateBookFormAddsBookFormAndAuthors() {
        ModelMap model = new ModelMap();
        List<Author> authors = List.of(new Author("Ronan", "OSullivan"));
        when(authorRepository.findAll()).thenReturn(authors);

        String viewName = controller.showCreateBookForm(model);

        assertEquals("book/addBook", viewName);
        assertEquals(BookForm.class, model.get("bookForm").getClass());
        assertSame(authors, model.get("authors"));
    }

    @Test
    void createBookReturnsFormViewAndRepopulatesAuthorsWhenValidationFails() {
        ModelMap model = new ModelMap();
        BookForm bookForm = new BookForm();
        BindingResult result = new BeanPropertyBindingResult(bookForm, "bookForm");
        result.reject("invalid");
        List<Author> authors = List.of(new Author("Ronan", "OSullivan"));
        when(authorRepository.findAll()).thenReturn(authors);

        String viewName = controller.createBook(bookForm, result, model);

        assertEquals("book/addBook", viewName);
        assertSame(authors, model.get("authors"));
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void createBookBuildsBookSavesAndRedirectsWhenValidationPasses() {
        ModelMap model = new ModelMap();
        BookForm bookForm = new BookForm();
        bookForm.setTitle("Lexicon Lair");
        bookForm.setAuthorId(7L);
        BindingResult result = new BeanPropertyBindingResult(bookForm, "bookForm");
        Author author = new Author("Ronan", "OSullivan");
        when(authorRepository.findById(7L)).thenReturn(Optional.of(author));

        String viewName = controller.createBook(bookForm, result, model);

        ArgumentCaptor<Book> savedBook = ArgumentCaptor.forClass(Book.class);
        assertEquals("redirect:list-books", viewName);
        verify(bookRepository).save(savedBook.capture());
        assertEquals("Lexicon Lair", savedBook.getValue().getTitle());
        assertSame(author, savedBook.getValue().getAuthor());
    }

    @Test
    void createBookThrowsWhenSelectedAuthorDoesNotExist() {
        ModelMap model = new ModelMap();
        BookForm bookForm = new BookForm();
        bookForm.setTitle("Lexicon Lair");
        bookForm.setAuthorId(7L);
        BindingResult result = new BeanPropertyBindingResult(bookForm, "bookForm");
        when(authorRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> controller.createBook(bookForm, result, model));
    }
}
