package com.ronanos.lexiconlair.dictionary;

import com.ronanos.lexiconlair.definition.domain.Definition;
import com.ronanos.lexiconlair.dictionary.dto.DictionaryWordDTO;
import com.ronanos.lexiconlair.dictionary.mapper.DefinitionMapper;
import com.ronanos.lexiconlair.word.domain.Word;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefinitionMapperTest {

    private final DefinitionMapper definitionMapper = new DefinitionMapper();

    @Test
    void mapToDefinitionsCreatesDefinitionsFromEntryMeanings() {
        Word word = new Word("lexicon", "en");
        DictionaryWordDTO entry = new DictionaryWordDTO();
        DictionaryWordDTO.MeaningDTO meaning = new DictionaryWordDTO.MeaningDTO();
        DictionaryWordDTO.MeaningDTO.DefinitionDTO definitionDTO = new DictionaryWordDTO.MeaningDTO.DefinitionDTO();
        definitionDTO.setDefinition("a dictionary or vocabulary");
        definitionDTO.setExample("The book includes a lexicon.");
        meaning.setPartOfSpeech("noun");
        meaning.setDefinitions(List.of(definitionDTO));
        entry.setMeanings(List.of(meaning));

        List<Definition> actual = definitionMapper.mapToDefinitions(word, entry);

        assertEquals(1, actual.size());
        Definition definition = actual.getFirst();
        assertSame(word, definition.getWord());
        assertEquals("noun", definition.getPartOfSpeech());
        assertEquals("a dictionary or vocabulary", definition.getDefinitionText());
        assertEquals("The book includes a lexicon.", definition.getExample());
        assertEquals("dictionaryapi.dev", definition.getSourceApi());
    }

    @Test
    void mapToDefinitionsReturnsEmptyListWhenEntryHasNoMeanings() {
        List<Definition> actual = definitionMapper.mapToDefinitions(new Word("lexicon", "en"), new DictionaryWordDTO());

        assertTrue(actual.isEmpty());
    }
}
