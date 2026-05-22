package com.ronanos.lexiconlair.word;

import com.ronanos.lexiconlair.admin.service.ExternalApiThrottleService;
import com.ronanos.lexiconlair.definition.domain.Definition;
import com.ronanos.lexiconlair.definition.persistence.DefinitionRepository;
import com.ronanos.lexiconlair.dictionary.client.DictionaryApiClient;
import com.ronanos.lexiconlair.dictionary.client.DictionaryLookupResult;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
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

    @Mock
    private ExternalApiThrottleService externalApiThrottleService;

    @InjectMocks
    private WordDefinitionService service;

    @Test
    void saveWordWithDefinitionsSavesWordThenMappedDefinitions() {
        Word word = new Word("lexicon", "en");
        DictionaryWordDTO entry = new DictionaryWordDTO();
        entry.setWord("lexicon");
        Definition definition = new Definition();
        when(wordRepository.save(word)).thenReturn(word);
        when(dictionaryApiClient.lookupEntries("lexicon", "en")).thenReturn(DictionaryLookupResult.success(List.of(entry)));
        when(definitionMapper.mapToDefinitions(word, entry, 1L)).thenReturn(List.of(definition));

        Word savedWord = service.saveWordWithDefinitions(word, 1L);

        assertSame(word, savedWord);
        verify(wordRepository, atLeastOnce()).save(word);
        verify(externalApiThrottleService).beforeExternalApiCall();
        verify(definitionRepository).saveAll(List.of(definition));
        assertEquals("SUCCESS", word.getDefinitionLookupStatus());
    }

    @Test
    void saveWordWithDefinitionsStillSavesWordWhenDictionaryReturnsNoEntries() {
        Word word = new Word("lexicon", "en");
        when(wordRepository.save(word)).thenReturn(word);
        when(dictionaryApiClient.lookupEntries("lexicon", "en"))
                .thenReturn(DictionaryLookupResult.noResults("Dictionary API returned no entries."));

        Word savedWord = service.saveWordWithDefinitions(word, 1L);

        assertSame(word, savedWord);
        verify(wordRepository, atLeastOnce()).save(word);
        verify(externalApiThrottleService).beforeExternalApiCall();
        verify(definitionRepository).saveAll(List.of());
        verify(definitionMapper, never()).mapToDefinitions(any(), any(), anyLong());
        assertEquals("NO_RESULTS", word.getDefinitionLookupStatus());
    }

    @Test
    void findOrCreateReturnsExistingWordWithoutCallingApi() {
        Word existing = new Word("lexicon", "en");
        when(wordRepository.findByTextIgnoreCaseAndLanguage("lexicon", "en"))
                .thenReturn(Optional.of(existing));

        Word result = service.findOrCreateWordWithDefinitions("lexicon", "en", 1L);

        assertSame(existing, result);
        verify(wordRepository, never()).save(any());
        verify(dictionaryApiClient, never()).lookupEntries(any(), any());
    }

    @Test
    void findOrCreateNormalizesTextToLowerCase() {
        Word existing = new Word("lexicon", "en");
        when(wordRepository.findByTextIgnoreCaseAndLanguage("lexicon", "en"))
                .thenReturn(Optional.of(existing));

        Word result = service.findOrCreateWordWithDefinitions("LEXICON", "en", 1L);

        assertSame(existing, result);
        verify(wordRepository).findByTextIgnoreCaseAndLanguage("lexicon", "en");
    }

    @Test
    void findOrCreateSavesNewWordAndFetchesDefinitionsWhenNotFound() {
        when(wordRepository.findByTextIgnoreCaseAndLanguage("ephemeral", "en"))
                .thenReturn(Optional.empty());
        Word savedWord = new Word("ephemeral", "en");
        when(wordRepository.save(any(Word.class))).thenReturn(savedWord);
        when(dictionaryApiClient.lookupEntries("ephemeral", "en"))
                .thenReturn(DictionaryLookupResult.noResults("Dictionary API returned no entries."));

        Word result = service.findOrCreateWordWithDefinitions("ephemeral", "en", 7L);

        assertSame(savedWord, result);
        verify(wordRepository, atLeastOnce()).save(any(Word.class));
        verify(dictionaryApiClient).lookupEntries("ephemeral", "en");
        verify(externalApiThrottleService).beforeExternalApiCall();
    }

    @Test
    void findOrCreateSetsCreatedByOnNewWord() {
        when(wordRepository.findByTextIgnoreCaseAndLanguage("ephemeral", "en"))
                .thenReturn(Optional.empty());
        when(wordRepository.save(any(Word.class))).thenAnswer(inv -> inv.getArgument(0));
        when(dictionaryApiClient.lookupEntries("ephemeral", "en"))
                .thenReturn(DictionaryLookupResult.noResults("Dictionary API returned no entries."));

        service.findOrCreateWordWithDefinitions("ephemeral", "en", 7L);

        verify(wordRepository, atLeastOnce()).save(argThat(word ->
                word.getCreatedBy().equals(7L) && word.getCreatedAt() != null));
    }

    @Test
    void refreshDefinitionsReplacesDefinitionsFromDictionary() {
        Word word = new Word("rakish", "en");
        DictionaryWordDTO entry = new DictionaryWordDTO();
        entry.setWord("rakish");
        Definition definition = new Definition();

        when(dictionaryApiClient.lookupEntries("rakish", "en")).thenReturn(DictionaryLookupResult.success(List.of(entry)));
        when(definitionMapper.mapToDefinitions(word, entry, 1L)).thenReturn(List.of(definition));

        List<Definition> result = service.refreshDefinitions(word, 1L);

        assertEquals(List.of(definition), result);
        verify(externalApiThrottleService).beforeExternalApiCall();
        verify(definitionRepository).deleteByWord_Id(word.getId());
        verify(definitionRepository).saveAll(List.of(definition));
        assertEquals("SUCCESS", word.getDefinitionLookupStatus());
    }

    @Test
    void refreshDefinitionsStoresFailureDetailsWhenDictionaryFails() {
        Word word = new Word("malady", "en");

        when(dictionaryApiClient.lookupEntries("malady", "en"))
                .thenReturn(DictionaryLookupResult.failure(429, "error code: 1015"));

        List<Definition> result = service.refreshDefinitions(word, 1L);

        assertEquals(List.of(), result);
        assertEquals("FAILED", word.getDefinitionLookupStatus());
        assertEquals(429, word.getDefinitionLookupHttpStatus());
        assertEquals("error code: 1015", word.getDefinitionLookupMessage());
    }

    private static <T> T argThat(org.mockito.ArgumentMatcher<T> matcher) {
        return org.mockito.ArgumentMatchers.argThat(matcher);
    }
}
