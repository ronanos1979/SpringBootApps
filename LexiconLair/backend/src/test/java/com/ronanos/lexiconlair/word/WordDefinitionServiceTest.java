package com.ronanos.lexiconlair.word;

import com.ronanos.lexiconlair.definition.domain.Definition;
import com.ronanos.lexiconlair.definition.persistence.DefinitionRepository;
import com.ronanos.lexiconlair.dictionary.client.DictionaryApiClient;
import com.ronanos.lexiconlair.dictionary.dto.DictionaryWordDTO;
import com.ronanos.lexiconlair.dictionary.mapper.DefinitionMapper;
import com.ronanos.lexiconlair.word.domain.Word;
import com.ronanos.lexiconlair.word.persistence.WordRepository;
import com.ronanos.lexiconlair.word.service.WordDefinitionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WordDefinitionServiceTest {

    @Mock
    private WordRepository wordRepository;

    @Mock
    private DefinitionRepository definitionRepository;

    @Mock
    private DictionaryApiClient dictionaryApiClient;

    @Mock
    private DefinitionMapper definitionMapper;

    @InjectMocks
    private WordDefinitionService service;

    @Test
    void saveWordWithDefinitionsSavesWordThenMappedDefinitions() {
        Word word = new Word("lexicon", "en");
        DictionaryWordDTO entry = new DictionaryWordDTO();
        entry.setWord("lexicon");
        Definition definition = new Definition();
        when(wordRepository.save(word)).thenReturn(word);
        when(dictionaryApiClient.getEntries("lexicon", "en")).thenReturn(List.of(entry));
        when(definitionMapper.mapToDefinitions(word, entry)).thenReturn(List.of(definition));

        Word savedWord = service.saveWordWithDefinitions(word);

        assertSame(word, savedWord);
        verify(wordRepository).save(word);
        verify(definitionRepository).saveAll(List.of(definition));
    }

    @Test
    void saveWordWithDefinitionsStillSavesWordWhenDictionaryReturnsNoEntries() {
        Word word = new Word("lexicon", "en");
        when(wordRepository.save(word)).thenReturn(word);
        when(dictionaryApiClient.getEntries("lexicon", "en")).thenReturn(List.of());

        Word savedWord = service.saveWordWithDefinitions(word);

        assertSame(word, savedWord);
        verify(wordRepository).save(word);
        verify(definitionRepository).saveAll(List.of());
        verify(definitionMapper, never()).mapToDefinitions(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}
