package com.ronanos.lexiconlair.word.web;

import java.util.List;

import com.ronanos.lexiconlair.word.domain.Word;
import com.ronanos.lexiconlair.word.dto.WordRequest;
import com.ronanos.lexiconlair.word.dto.WordResponse;
import com.ronanos.lexiconlair.word.persistence.WordRepository;
import com.ronanos.lexiconlair.word.service.WordDefinitionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/words")
public class WordController {

    private static final Logger logger = LoggerFactory.getLogger(WordController.class);

    private final WordRepository wordRepository;
    private final WordDefinitionService wordDefinitionService;

    public WordController(WordRepository wordRepository, WordDefinitionService wordDefinitionService) {
        this.wordRepository = wordRepository;
        this.wordDefinitionService = wordDefinitionService;
    }


    @GetMapping
    public List<WordResponse> listWords() {
        return wordRepository.findAll().stream()
                .map(WordResponse::from)
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
        Word word = new Word(request.text(), request.language());
        Word saved = wordDefinitionService.saveWordWithDefinitions(word);
        logger.info("Created word {}", saved.getText());
        return WordResponse.from(saved);
    }

    @PutMapping("/{id}")
    public WordResponse updateWord(@PathVariable Long id, @Valid @RequestBody WordRequest request) {
        Word existing = wordRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Word not found"));
        existing.setText(request.text());
        existing.setLanguage(request.language());
        Word saved = wordRepository.save(existing);
        logger.info("Updated word {}", saved.getText());
        return WordResponse.from(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWord(@PathVariable Long id) {
        wordRepository.deleteById(id);
    }
}
