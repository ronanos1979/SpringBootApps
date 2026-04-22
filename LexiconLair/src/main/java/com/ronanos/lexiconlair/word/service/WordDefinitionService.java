package com.ronanos.lexiconlair.word.service;

import com.ronanos.lexiconlair.definition.domain.Definition;
import com.ronanos.lexiconlair.definition.persistence.DefinitionRepository;
import com.ronanos.lexiconlair.dictionary.client.DictionaryApiClient;
import com.ronanos.lexiconlair.dictionary.dto.DictionaryWordDTO;
import com.ronanos.lexiconlair.dictionary.mapper.DefinitionMapper;
import com.ronanos.lexiconlair.word.domain.Word;
import com.ronanos.lexiconlair.word.persistence.WordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class WordDefinitionService {

    private final WordRepository wordRepository;
    private final DefinitionRepository definitionRepository;
    private final DictionaryApiClient dictionaryApiClient;
    private final DefinitionMapper definitionMapper;

    public WordDefinitionService(
            WordRepository wordRepository,
            DefinitionRepository definitionRepository,
            DictionaryApiClient dictionaryApiClient,
            DefinitionMapper definitionMapper) {
        this.wordRepository = wordRepository;
        this.definitionRepository = definitionRepository;
        this.dictionaryApiClient = dictionaryApiClient;
        this.definitionMapper = definitionMapper;
    }

    @Transactional
    public Word saveWordWithDefinitions(Word word) {
        Word savedWord = wordRepository.save(word);

        List<DictionaryWordDTO> dictionaryEntries =
                dictionaryApiClient.getEntries(savedWord.getText(), savedWord.getLanguage());
        List<Definition> definitions = new ArrayList<>();

        for (DictionaryWordDTO entry : dictionaryEntries) {
            definitions.addAll(definitionMapper.mapToDefinitions(savedWord, entry));
        }

        definitionRepository.saveAll(definitions);
        return savedWord;
    }
}
