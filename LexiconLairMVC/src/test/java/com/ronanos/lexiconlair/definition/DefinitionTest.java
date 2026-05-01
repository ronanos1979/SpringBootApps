package com.ronanos.lexiconlair.definition;

import com.ronanos.lexiconlair.definition.domain.Definition;
import com.ronanos.lexiconlair.word.domain.Word;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefinitionTest {

    @Test
    void gettersSettersAndToStringExposeDefinitionState() {
        Definition definition = new Definition();
        Word word = new Word("lexicon", "en");
        LocalDateTime cachedAt = LocalDateTime.of(2026, 4, 22, 13, 0);

        definition.setId(7L);
        definition.setWord(word);
        definition.setDefinitionText("a dictionary or vocabulary");
        definition.setPartOfSpeech("noun");
        definition.setExample("The book includes a lexicon.");
        definition.setSourceApi("dictionaryapi.dev");
        definition.setCachedAt(cachedAt);

        assertEquals(7L, definition.getId());
        assertSame(word, definition.getWord());
        assertEquals("a dictionary or vocabulary", definition.getDefinitionText());
        assertEquals("noun", definition.getPartOfSpeech());
        assertEquals("The book includes a lexicon.", definition.getExample());
        assertEquals("dictionaryapi.dev", definition.getSourceApi());
        assertEquals(cachedAt, definition.getCachedAt());
        assertTrue(definition.toString().contains("dictionaryapi.dev"));
        assertTrue(definition.toString().contains("definitionText='a dictionary or vocabulary'"));
    }
}
