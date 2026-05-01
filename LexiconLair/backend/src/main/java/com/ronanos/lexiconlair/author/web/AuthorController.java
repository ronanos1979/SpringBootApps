package com.ronanos.lexiconlair.author.web;

import java.util.List;

import com.ronanos.lexiconlair.author.domain.Author;
import com.ronanos.lexiconlair.author.dto.AuthorRequest;
import com.ronanos.lexiconlair.author.dto.AuthorResponse;
import com.ronanos.lexiconlair.author.persistence.AuthorRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private static final Logger logger = LoggerFactory.getLogger(AuthorController.class);

    private final AuthorRepository authorRepository;

    public AuthorController(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @GetMapping
    public List<AuthorResponse> listAuthors() {
        return authorRepository.findAll().stream()
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
        Author author = new Author(request.firstName(), request.lastName());
        Author saved = authorRepository.save(author);
        logger.info("Created author {} {}", saved.getFirstName(), saved.getLastName());
        return AuthorResponse.from(saved);
    }

    @PutMapping("/{id}")
    public AuthorResponse updateAuthor(@PathVariable Long id, @Valid @RequestBody AuthorRequest request) {
        Author existing = authorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found"));
        existing.setFirstName(request.firstName());
        existing.setLastName(request.lastName());
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
}
