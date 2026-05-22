package com.ronanos.lexiconlair.word;

import com.ronanos.lexiconlair.word.domain.Word;
import com.ronanos.lexiconlair.word.dto.WordResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class WordDtoTest {

    @Test
    void wordResponseMapsDomainFields() {
        Word word = new Word("lexicon", "en");
        ReflectionTestUtils.setField(word, "id", 1L);

        WordResponse response = WordResponse.from(word);

        assertEquals(1L, response.id());
        assertEquals("lexicon", response.text());
        assertEquals("en", response.language());
    }

    @Test
    void wordResponseMapsAuditFields() {
        Word word = new Word("lexicon", "en");
        LocalDateTime now = LocalDateTime.of(2026, 5, 22, 10, 0);
        word.setCreatedAt(now);
        word.setCreatedBy(5L);
        word.setUpdatedAt(now.plusHours(2));
        word.setUpdatedBy(6L);

        WordResponse response = WordResponse.from(word);

        assertEquals(now, response.createdAt());
        assertEquals(5L, response.createdBy());
        assertEquals(now.plusHours(2), response.updatedAt());
        assertEquals(6L, response.updatedBy());
    }

    @Test
    void wordResponseHasNullAuditFieldsWhenNotSet() {
        Word word = new Word("lexicon", "en");

        WordResponse response = WordResponse.from(word);

        assertNull(response.createdAt());
        assertNull(response.createdBy());
        assertNull(response.updatedAt());
        assertNull(response.updatedBy());
    }

    @Test
    void wordResponseMapsDefinitionLookupFields() {
        Word word = new Word("malady", "en");
        LocalDateTime now = LocalDateTime.of(2026, 5, 22, 11, 0);
        word.setDefinitionLookupStatus("FAILED");
        word.setDefinitionLookupHttpStatus(429);
        word.setDefinitionLookupMessage("error code: 1015");
        word.setDefinitionLookupAt(now);

        WordResponse response = WordResponse.from(word);

        assertEquals("FAILED", response.definitionLookupStatus());
        assertEquals(429, response.definitionLookupHttpStatus());
        assertEquals("error code: 1015", response.definitionLookupMessage());
        assertEquals(now, response.definitionLookupAt());
    }
}
