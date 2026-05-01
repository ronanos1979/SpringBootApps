package com.ronanos.lexiconlair.word;

import com.ronanos.lexiconlair.word.domain.Word;
import com.ronanos.lexiconlair.word.dto.WordResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
