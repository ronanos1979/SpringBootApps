package com.ronanos.lexiconlair.word.service;

import com.ronanos.lexiconlair.admin.service.ExternalApiThrottleService;
import com.ronanos.lexiconlair.definition.domain.Definition;
import com.ronanos.lexiconlair.definition.persistence.DefinitionRepository;
import com.ronanos.lexiconlair.dictionary.client.DictionaryApiClient;
import com.ronanos.lexiconlair.dictionary.client.DictionaryLookupResult;
import com.ronanos.lexiconlair.dictionary.dto.DictionaryWordDTO;
import com.ronanos.lexiconlair.dictionary.mapper.DefinitionMapper;
import com.ronanos.lexiconlair.word.domain.Word;
import com.ronanos.lexiconlair.word.persistence.WordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class WordDefinitionService {

    private final WordRepository wordRepository;
    private final DefinitionRepository definitionRepository;
    private final DictionaryApiClient dictionaryApiClient;
    private final DefinitionMapper definitionMapper;
    private final ExternalApiThrottleService externalApiThrottleService;

    public WordDefinitionService(
            WordRepository wordRepository,
            DefinitionRepository definitionRepository,
            DictionaryApiClient dictionaryApiClient,
            DefinitionMapper definitionMapper,
            ExternalApiThrottleService externalApiThrottleService) {
        this.wordRepository = wordRepository;
        this.definitionRepository = definitionRepository;
        this.dictionaryApiClient = dictionaryApiClient;
        this.definitionMapper = definitionMapper;
        this.externalApiThrottleService = externalApiThrottleService;
    }

    @Transactional
    public Word findOrCreateWordWithDefinitions(String text, String language, Long createdBy) {
        String normalizedText = text.trim().toLowerCase();
        return wordRepository.findByTextIgnoreCaseAndLanguage(normalizedText, language)
                .orElseGet(() -> {
                    Word word = new Word(normalizedText, language);
                    word.setCreatedAt(LocalDateTime.now());
                    word.setCreatedBy(createdBy);
                    return saveWordWithDefinitions(word, createdBy);
                });
    }

    @Transactional
    public Word saveWordWithDefinitions(Word word, Long createdBy) {
        Word savedWord = wordRepository.save(word);
        refreshDefinitions(savedWord, createdBy);
        return savedWord;
    }

    @Transactional
    public List<Definition> refreshDefinitions(Word word, Long updatedBy) {
        externalApiThrottleService.beforeExternalApiCall();
        DictionaryLookupResult lookupResult = dictionaryApiClient.lookupEntries(word.getText(), word.getLanguage());
        List<DictionaryWordDTO> dictionaryEntries = lookupResult.entries();
        List<Definition> definitions = new ArrayList<>();

        for (DictionaryWordDTO entry : dictionaryEntries) {
            definitions.addAll(definitionMapper.mapToDefinitions(word, entry, updatedBy));
        }

        definitionRepository.deleteByWord_Id(word.getId());
        definitionRepository.saveAll(definitions);
        updateDefinitionLookupState(word, lookupResult, definitions);
        return definitions;
    }

    private void updateDefinitionLookupState(
            Word word,
            DictionaryLookupResult lookupResult,
            List<Definition> definitions) {

        if (!definitions.isEmpty()) {
            word.setDefinitionLookupStatus("SUCCESS");
            word.setDefinitionLookupHttpStatus(lookupResult.httpStatus());
            word.setDefinitionLookupMessage(null);
        } else if ("SUCCESS".equals(lookupResult.status())) {
            word.setDefinitionLookupStatus("NO_DEFINITIONS");
            word.setDefinitionLookupHttpStatus(lookupResult.httpStatus());
            word.setDefinitionLookupMessage("Dictionary API returned entries, but no definitions could be mapped.");
        } else {
            word.setDefinitionLookupStatus(lookupResult.status());
            word.setDefinitionLookupHttpStatus(lookupResult.httpStatus());
            word.setDefinitionLookupMessage(lookupResult.message());
        }

        word.setDefinitionLookupAt(LocalDateTime.now());
        wordRepository.save(word);
    }
}
