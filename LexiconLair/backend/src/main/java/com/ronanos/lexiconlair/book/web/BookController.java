package com.ronanos.lexiconlair.book.web;

import com.ronanos.lexiconlair.author.domain.Author;
import com.ronanos.lexiconlair.author.persistence.AuthorRepository;
import com.ronanos.lexiconlair.book.domain.Book;
import com.ronanos.lexiconlair.book.dto.BookRequest;
import com.ronanos.lexiconlair.book.dto.BookResponse;
import com.ronanos.lexiconlair.book.persistence.BookRepository;
import com.ronanos.lexiconlair.user.domain.User;
import com.ronanos.lexiconlair.user.persistence.UserRepository;
import com.ronanos.lexiconlair.userbook.domain.UserBook;
import com.ronanos.lexiconlair.userbook.persistence.UserBookRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final UserRepository userRepository;
    private final UserBookRepository userBookRepository;

    public BookController(BookRepository bookRepository, AuthorRepository authorRepository,
                          UserRepository userRepository, UserBookRepository userBookRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.userRepository = userRepository;
        this.userBookRepository = userBookRepository;
    }

    @GetMapping
    public List<BookResponse> listBooks(@RequestParam(defaultValue = "false") boolean mine) {
        if (mine) {
            Long userId = getCurrentUserId();
            Set<Long> seen = new LinkedHashSet<>();
            List<Book> result = new ArrayList<>();

            bookRepository.findByCreatedByOrderByTitleAsc(userId).forEach(b -> {
                if (seen.add(b.getId())) result.add(b);
            });

            List<Long> savedIds = userBookRepository.findBookIdsByUserId(userId);
            if (savedIds != null && !savedIds.isEmpty()) {
                bookRepository.findAllById(savedIds).forEach(b -> {
                    if (seen.add(b.getId())) result.add(b);
                });
            }

            result.sort(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER));
            return result.stream().map(BookResponse::from).toList();
        }
        return bookRepository.findAll().stream()
                .map(BookResponse::from)
                .toList();
    }

    @GetMapping("/search")
    public List<BookResponse> searchBooks(@RequestParam String q) {
        if (q == null || q.isBlank()) {
            return List.of();
        }
        return bookRepository.findTop10ByTitleContainingIgnoreCaseOrderByTitleAsc(q.trim()).stream()
                .map(BookResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public BookResponse getBook(@PathVariable Long id) {
        return bookRepository.findById(id)
                .map(BookResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse createBook(@Valid @RequestBody BookRequest request) {
        bookRepository.findByTitleIgnoreCase(request.title())
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "A book with this title already exists");
                });

        Author author = authorRepository.findById(request.authorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid author id"));

        Book book = new Book();
        book.setTitle(request.title());
        book.setAuthor(author);

        Long userId = getCurrentUserId();
        book.setCreatedAt(LocalDateTime.now());
        book.setCreatedBy(userId);

        Book saved = bookRepository.save(book);
        logger.info("Created book {} {}", saved.getTitle(), saved.getAuthor().getFirstName());

        if (userId != null && saved.getId() != null) {
            userBookRepository.save(new UserBook(userId, saved.getId()));
        }

        return BookResponse.from(saved);
    }

    @PostMapping("/{id}/save")
    public BookResponse saveToCollection(@PathVariable Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
        Long userId = getCurrentUserId();
        if (userId != null && !userBookRepository.existsByUserIdAndBookId(userId, id)) {
            userBookRepository.save(new UserBook(userId, id));
        }
        return BookResponse.from(book);
    }

    @PutMapping("/{id}")
    public BookResponse updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        Book existing = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
        bookRepository.findByTitleIgnoreCase(request.title())
                .filter(found -> !found.getId().equals(id))
                .ifPresent(found -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "A book with this title already exists");
                });
        Author author = authorRepository.findById(request.authorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid author id"));
        existing.setTitle(request.title());
        existing.setAuthor(author);
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(getCurrentUserId());
        Book saved = bookRepository.save(existing);
        logger.info("Updated book {} {}", saved.getTitle(), saved.getAuthor().getFirstName());
        return BookResponse.from(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable Long id) {
        bookRepository.deleteById(id);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName())
                .map(User::getId)
                .orElse(null);
    }
}
