package com.ronanos.lexiconlair.book.web;

import com.ronanos.lexiconlair.author.domain.Author;
import com.ronanos.lexiconlair.author.persistence.AuthorRepository;
import com.ronanos.lexiconlair.book.domain.Book;
import com.ronanos.lexiconlair.book.persistence.BookRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class BookController {
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    private BookRepository bookRepository;
    private AuthorRepository authorRepository;


    public BookController(BookRepository bookRepository, AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
    }

    @GetMapping("list-books")
    public String listAllBooks(ModelMap model) {
        List<Book> bookList = bookRepository.findAll();
        model.addAttribute("books", bookList);
        return "book/listBooks";
    }

    @GetMapping("add-book")
    public String showCreateBookForm(ModelMap model) {
        model.addAttribute("bookForm", new BookForm());
        model.addAttribute("authors", authorRepository.findAll());
        return "book/addBook";
    }

    @PostMapping("add-book")
    public String createBook(@Valid @ModelAttribute("bookForm") BookForm bookForm, BindingResult result, ModelMap model) {
        if (result.hasErrors()) {
            model.addAttribute("authors", authorRepository.findAll());
            return "book/addBook";
        }

        Author author = authorRepository.findById(bookForm.getAuthorId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid author id: " + bookForm.getAuthorId()));

        Book book = new Book();
        book.setTitle(bookForm.getTitle());
        book.setAuthor(author);
        bookRepository.save(book);
        logger.info("Created book {} {}", book.getTitle(), book.getAuthor().getFirstName());
        return "redirect:list-books";

    }

}
