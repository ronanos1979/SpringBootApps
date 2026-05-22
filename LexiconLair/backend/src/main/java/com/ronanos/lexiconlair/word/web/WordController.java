package com.ronanos.lexiconlair.word.web;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.ronanos.lexiconlair.bookword.dto.WordSearchResult;
import com.ronanos.lexiconlair.bookword.domain.BookWord;
import com.ronanos.lexiconlair.definition.domain.Definition;
import com.ronanos.lexiconlair.bookword.persistence.BookWordRepository;
import com.ronanos.lexiconlair.definition.persistence.DefinitionRepository;
import com.ronanos.lexiconlair.user.domain.User;
import com.ronanos.lexiconlair.user.persistence.UserRepository;
import com.ronanos.lexiconlair.word.domain.Word;
import com.ronanos.lexiconlair.word.dto.WordRequest;
import com.ronanos.lexiconlair.word.dto.WordResponse;
import com.ronanos.lexiconlair.word.persistence.WordRepository;
import com.ronanos.lexiconlair.word.service.WordDefinitionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/words")
public class WordController {

    private static final Logger logger = LoggerFactory.getLogger(WordController.class);

    private final WordRepository wordRepository;
    private final WordDefinitionService wordDefinitionService;
    private final UserRepository userRepository;
    private final BookWordRepository bookWordRepository;
    private final DefinitionRepository definitionRepository;

    public WordController(
            WordRepository wordRepository,
            WordDefinitionService wordDefinitionService,
            UserRepository userRepository,
            BookWordRepository bookWordRepository,
            DefinitionRepository definitionRepository) {
        this.wordRepository = wordRepository;
        this.wordDefinitionService = wordDefinitionService;
        this.userRepository = userRepository;
        this.bookWordRepository = bookWordRepository;
        this.definitionRepository = definitionRepository;
    }

    @GetMapping
    public List<WordResponse> listWords() {
        return wordRepository.findAll().stream()
                .map(WordResponse::from)
                .toList();
    }

    @GetMapping("/search")
    public List<WordSearchResult> searchWords(@RequestParam(defaultValue = "") String q) {
        List<BookWord> bookWords = bookWordRepository.searchAll(q);
        Set<Long> wordIdsWithBookContext = new HashSet<>();

        List<WordSearchResult> bookWordResults = bookWords.stream()
                .peek(bw -> wordIdsWithBookContext.add(bw.getWord().getId()))
                .map(bw -> WordSearchResult.from(bw, definitionRepository.findByWord_Id(bw.getWord().getId())))
                .toList();

        List<WordSearchResult> savedWordResults = wordRepository.searchByText(q).stream()
                .filter(word -> !wordIdsWithBookContext.contains(word.getId()))
                .map(word -> WordSearchResult.from(word, definitionRepository.findByWord_Id(word.getId())))
                .toList();

        return Stream.concat(bookWordResults.stream(), savedWordResults.stream()).toList();
    }

    @GetMapping("/without-definitions")
    public List<WordResponse> listWordsWithoutDefinitions() {
        return wordRepository.findWithoutDefinitions().stream()
                .map(WordResponse::from)
                .toList();
    }

    @PostMapping("/{id}/definitions/refresh")
    public WordSearchResult refreshWordDefinitions(@PathVariable Long id) {
        Word word = wordRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Word not found"));
        List<Definition> definitions = wordDefinitionService.refreshDefinitions(word, getCurrentUserId());
        return WordSearchResult.from(word, definitions);
    }

    @PostMapping("/definitions/refresh-missing")
    public List<WordSearchResult> refreshMissingDefinitions() {
        Long currentUserId = getCurrentUserId();
        return wordRepository.findWithoutDefinitions().stream()
                .map(word -> WordSearchResult.from(word, wordDefinitionService.refreshDefinitions(word, currentUserId)))
                .toList();
    }

    @GetMapping("/{id}")
    public WordResponse getWord(@PathVariable Long id) {
        return wordRepository.findById(id)
                .map(WordResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Word not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WordResponse createWord(@Valid @RequestBody WordRequest request) {
        Long currentUserId = getCurrentUserId();
        Word word = new Word(request.text(), request.language());
        word.setCreatedAt(LocalDateTime.now());
        word.setCreatedBy(currentUserId);
        Word saved = wordDefinitionService.saveWordWithDefinitions(word, currentUserId);
        logger.info("Created word {}", saved.getText());
        return WordResponse.from(saved);
    }

    @PutMapping("/{id}")
    public WordResponse updateWord(@PathVariable Long id, @Valid @RequestBody WordRequest request) {
        Word existing = wordRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Word not found"));
        existing.setText(request.text());
        existing.setLanguage(request.language());
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(getCurrentUserId());
        Word saved = wordRepository.save(existing);
        logger.info("Updated word {}", saved.getText());
        return WordResponse.from(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWord(@PathVariable Long id) {
        wordRepository.deleteById(id);
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
