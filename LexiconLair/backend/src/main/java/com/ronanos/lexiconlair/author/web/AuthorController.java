package com.ronanos.lexiconlair.author.web;

import com.ronanos.lexiconlair.author.domain.Author;
import com.ronanos.lexiconlair.author.dto.AuthorRequest;
import com.ronanos.lexiconlair.author.dto.AuthorResponse;
import com.ronanos.lexiconlair.author.persistence.AuthorRepository;
import com.ronanos.lexiconlair.user.domain.User;
import com.ronanos.lexiconlair.user.persistence.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private static final Logger logger = LoggerFactory.getLogger(AuthorController.class);

    private final AuthorRepository authorRepository;
    private final UserRepository userRepository;

    public AuthorController(AuthorRepository authorRepository, UserRepository userRepository) {
        this.authorRepository = authorRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<AuthorResponse> listAuthors() {
        return authorRepository.findAll().stream()
                .map(AuthorResponse::from)
                .toList();
    }

    @GetMapping("/search")
    public List<AuthorResponse> searchAuthors(@RequestParam String q) {
        if (q == null || q.isBlank()) {
            return List.of();
        }
        return authorRepository.searchByDisplayName(q.trim()).stream()
                .limit(10)
                .map(AuthorResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public AuthorResponse getAuthor(@PathVariable Long id) {
        return authorRepository.findById(id)
                .map(AuthorResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorResponse createAuthor(@Valid @RequestBody AuthorRequest request) {
        authorRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase(request.firstName(), request.lastName())
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Author already exists");
                });
        Author author = new Author(request.firstName(), request.lastName());
        author.setCreatedAt(LocalDateTime.now());
        author.setCreatedBy(getCurrentUserId());
        Author saved = authorRepository.save(author);
        logger.info("Created author {} {}", saved.getFirstName(), saved.getLastName());
        return AuthorResponse.from(saved);
    }

    @PutMapping("/{id}")
    public AuthorResponse updateAuthor(@PathVariable Long id, @Valid @RequestBody AuthorRequest request) {
        Author existing = authorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found"));
        authorRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase(request.firstName(), request.lastName())
                .filter(found -> !found.getId().equals(id))
                .ifPresent(found -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Author already exists");
                });
        existing.setFirstName(request.firstName());
        existing.setLastName(request.lastName());
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(getCurrentUserId());
        Author saved = authorRepository.save(existing);
        logger.info("Updated author {} {}", saved.getFirstName(), saved.getLastName());
        return AuthorResponse.from(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAuthor(@PathVariable Long id) {
        logger.info("Deleting author with id {}", id);
        authorRepository.deleteById(id);
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
