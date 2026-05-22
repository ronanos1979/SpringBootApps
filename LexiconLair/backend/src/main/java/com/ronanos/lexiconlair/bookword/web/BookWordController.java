package com.ronanos.lexiconlair.bookword.web;

import com.ronanos.lexiconlair.book.domain.Book;
import com.ronanos.lexiconlair.book.persistence.BookRepository;
import com.ronanos.lexiconlair.bookword.domain.BookWord;
import com.ronanos.lexiconlair.bookword.dto.AddWordToBookRequest;
import com.ronanos.lexiconlair.bookword.dto.BookWordResponse;
import com.ronanos.lexiconlair.bookword.dto.BulkAddWordsRequest;
import com.ronanos.lexiconlair.bookword.dto.BulkAddWordsResponse;
import com.ronanos.lexiconlair.bookword.persistence.BookWordRepository;
import com.ronanos.lexiconlair.definition.domain.Definition;
import com.ronanos.lexiconlair.definition.persistence.DefinitionRepository;
import com.ronanos.lexiconlair.user.domain.User;
import com.ronanos.lexiconlair.user.persistence.UserRepository;
import com.ronanos.lexiconlair.word.domain.Word;
import com.ronanos.lexiconlair.word.service.WordDefinitionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/books/{bookId}/words")
public class BookWordController {

    private static final Logger logger = LoggerFactory.getLogger(BookWordController.class);

    private final BookWordRepository bookWordRepository;
    private final BookRepository bookRepository;
    private final DefinitionRepository definitionRepository;
    private final WordDefinitionService wordDefinitionService;
    private final UserRepository userRepository;

    public BookWordController(
            BookWordRepository bookWordRepository,
            BookRepository bookRepository,
            DefinitionRepository definitionRepository,
            WordDefinitionService wordDefinitionService,
            UserRepository userRepository) {
        this.bookWordRepository = bookWordRepository;
        this.bookRepository = bookRepository;
        this.definitionRepository = definitionRepository;
        this.wordDefinitionService = wordDefinitionService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<BookWordResponse> listWordsForBook(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "true") boolean mine) {

        if (!bookRepository.existsById(bookId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
        }

        List<BookWord> bookWords = mine
                ? bookWordRepository.findByBook_IdAndCreatedByOrderByCreatedAtDesc(bookId, getCurrentUserId())
                : bookWordRepository.findByBook_IdOrderByCreatedAtDesc(bookId);

        return bookWords.stream()
                .map(bw -> {
                    List<Definition> defs = definitionRepository.findByWord_Id(bw.getWord().getId());
                    return BookWordResponse.from(bw, defs);
                })
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookWordResponse addWordToBook(
            @PathVariable Long bookId,
            @Valid @RequestBody AddWordToBookRequest request) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

        Long userId = getCurrentUserId();
        Word word = wordDefinitionService.findOrCreateWordWithDefinitions(request.text(), request.language(), userId);

        if (bookWordRepository.existsByBook_IdAndWord_Id(bookId, word.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Word already added to this book");
        }

        BookWord bookWord = new BookWord();
        bookWord.setBook(book);
        bookWord.setWord(word);
        bookWord.setCreatedAt(LocalDateTime.now());
        bookWord.setCreatedBy(userId);
        BookWord saved = bookWordRepository.save(bookWord);

        List<Definition> definitions = definitionRepository.findByWord_Id(word.getId());
        logger.info("Added word '{}' to book '{}'", word.getText(), book.getTitle());
        return BookWordResponse.from(saved, definitions);
    }

    @PostMapping("/bulk")
    public BulkAddWordsResponse bulkAddWordsToBook(
            @PathVariable Long bookId,
            @Valid @RequestBody BulkAddWordsRequest request) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

        Long userId = getCurrentUserId();

        List<String> cleanedWords = request.words().stream()
                .map(String::trim)
                .filter(w -> !w.isEmpty())
                .distinct()
                .toList();

        List<BookWordResponse> added = new ArrayList<>();
        List<String> duplicates = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (String wordText : cleanedWords) {
            try {
                Word word = wordDefinitionService.findOrCreateWordWithDefinitions(wordText, request.language(), userId);

                if (bookWordRepository.existsByBook_IdAndWord_Id(bookId, word.getId())) {
                    duplicates.add(wordText);
                    continue;
                }

                BookWord bookWord = new BookWord();
                bookWord.setBook(book);
                bookWord.setWord(word);
                bookWord.setCreatedAt(LocalDateTime.now());
                bookWord.setCreatedBy(userId);
                BookWord saved = bookWordRepository.save(bookWord);

                List<Definition> definitions = definitionRepository.findByWord_Id(word.getId());
                added.add(BookWordResponse.from(saved, definitions));
            } catch (Exception e) {
                errors.add(wordText);
                logger.warn("Bulk add: failed word '{}' for book {}: {}", wordText, bookId, e.getMessage());
            }
        }

        logger.info("Bulk add to book {}: {} added, {} duplicates, {} errors",
                bookId, added.size(), duplicates.size(), errors.size());
        return new BulkAddWordsResponse(added, duplicates, errors);
    }

    @DeleteMapping("/{bookWordId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeWordFromBook(@PathVariable Long bookId, @PathVariable Long bookWordId) {
        BookWord bookWord = bookWordRepository.findById(bookWordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found"));

        if (!bookWord.getBook().getId().equals(bookId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Entry does not belong to this book");
        }

        bookWordRepository.deleteById(bookWordId);
        logger.info("Removed book-word entry {}", bookWordId);
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
