package com.ronanos.lexiconlair.definition;

import com.ronanos.lexiconlair.definition.domain.Definition;
import com.ronanos.lexiconlair.definition.dto.DefinitionResponse;
import com.ronanos.lexiconlair.word.domain.Word;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefinitionDtoTest {

    @Test
    void definitionResponseMapsNestedWordDto() {
        Word word = new Word("lexicon", "en");
        ReflectionTestUtils.setField(word, "id", 2L);
        Definition definition = new Definition();
        ReflectionTestUtils.setField(definition, "id", 1L);
        definition.setWord(word);
        definition.setDefinitionText("A vocabulary.");
        definition.setPartOfSpeech("noun");
        definition.setExample("The lexicon is broad.");
        definition.setSourceApi("dictionary-api");
        definition.setCachedAt(LocalDateTime.of(2026, 5, 1, 11, 0));

        DefinitionResponse response = DefinitionResponse.from(definition);

        assertEquals(1L, response.id());
        assertEquals(2L, response.word().id());
        assertEquals("lexicon", response.word().text());
        assertEquals("A vocabulary.", response.definitionText());
        assertEquals("noun", response.partOfSpeech());
        assertEquals("The lexicon is broad.", response.example());
        assertEquals("dictionary-api", response.sourceApi());
        assertEquals(LocalDateTime.of(2026, 5, 1, 11, 0), response.cachedAt());
    }
}
