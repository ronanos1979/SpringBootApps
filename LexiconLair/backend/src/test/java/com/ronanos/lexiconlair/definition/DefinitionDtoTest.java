package com.ronanos.lexiconlair.definition;

import com.ronanos.lexiconlair.definition.domain.Definition;
import com.ronanos.lexiconlair.definition.dto.DefinitionResponse;
import com.ronanos.lexiconlair.word.domain.Word;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @Test
    void definitionResponseMapsAuditFields() {
        Definition definition = new Definition();
        definition.setWord(new Word("lexicon", "en"));
        definition.setDefinitionText("A vocabulary.");
        definition.setPartOfSpeech("noun");
        definition.setSourceApi("dictionary-api");
        LocalDateTime now = LocalDateTime.of(2026, 5, 22, 10, 0);
        definition.setCreatedAt(now);
        definition.setCreatedBy(9L);
        definition.setUpdatedAt(now.plusHours(3));
        definition.setUpdatedBy(10L);

        DefinitionResponse response = DefinitionResponse.from(definition);

        assertEquals(now, response.createdAt());
        assertEquals(9L, response.createdBy());
        assertEquals(now.plusHours(3), response.updatedAt());
        assertEquals(10L, response.updatedBy());
    }

    @Test
    void definitionResponseHasNullAuditFieldsWhenNotSet() {
        Definition definition = new Definition();
        definition.setWord(new Word("lexicon", "en"));
        definition.setDefinitionText("A vocabulary.");
        definition.setPartOfSpeech("noun");
        definition.setSourceApi("dictionary-api");

        DefinitionResponse response = DefinitionResponse.from(definition);

        assertNull(response.createdAt());
        assertNull(response.createdBy());
        assertNull(response.updatedAt());
        assertNull(response.updatedBy());
    }
}
