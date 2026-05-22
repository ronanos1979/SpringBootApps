package com.ronanos.lexiconlair.definition.web;

import com.ronanos.lexiconlair.definition.domain.Definition;
import com.ronanos.lexiconlair.definition.dto.DefinitionRequest;
import com.ronanos.lexiconlair.definition.dto.DefinitionResponse;
import com.ronanos.lexiconlair.definition.persistence.DefinitionRepository;
import com.ronanos.lexiconlair.user.domain.User;
import com.ronanos.lexiconlair.user.persistence.UserRepository;
import com.ronanos.lexiconlair.word.domain.Word;
import com.ronanos.lexiconlair.word.persistence.WordRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/definitions")
public class DefinitionController {
    private final DefinitionRepository definitionRepository;
    private final WordRepository wordRepository;
    private final UserRepository userRepository;

    public DefinitionController(DefinitionRepository definitionRepository, WordRepository wordRepository, UserRepository userRepository) {
        this.definitionRepository = definitionRepository;
        this.wordRepository = wordRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<DefinitionResponse> listDefinitions() {
        return definitionRepository.findAll().stream()
                .map(DefinitionResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public DefinitionResponse getDefinition(@PathVariable Long id) {
        return definitionRepository.findById(id)
                .map(DefinitionResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Definition not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DefinitionResponse createDefinition(@Valid @RequestBody DefinitionRequest request) {
        Definition definition = new Definition();
        applyRequest(definition, request);
        definition.setCreatedAt(LocalDateTime.now());
        definition.setCreatedBy(getCurrentUserId());
        return DefinitionResponse.from(definitionRepository.save(definition));
    }

    @PutMapping("/{id}")
    public DefinitionResponse updateDefinition(@PathVariable Long id, @Valid @RequestBody DefinitionRequest request) {
        Definition definition = definitionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Definition not found"));
        applyRequest(definition, request);
        definition.setUpdatedAt(LocalDateTime.now());
        definition.setUpdatedBy(getCurrentUserId());
        return DefinitionResponse.from(definitionRepository.save(definition));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDefinition(@PathVariable Long id) {
        definitionRepository.deleteById(id);
    }

    private void applyRequest(Definition definition, DefinitionRequest request) {
        Word word = wordRepository.findById(request.wordId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid word id"));
        definition.setWord(word);
        definition.setDefinitionText(request.definitionText());
        definition.setPartOfSpeech(request.partOfSpeech());
        definition.setExample(request.example());
        definition.setSourceApi(request.sourceApi());
        definition.setCachedAt(request.cachedAt());
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
