package com.ronanos.lexiconlair.definition;

import com.ronanos.lexiconlair.definition.domain.Definition;
import com.ronanos.lexiconlair.definition.persistence.DefinitionRepository;
import com.ronanos.lexiconlair.word.domain.Word;
import com.ronanos.lexiconlair.word.persistence.WordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class DefinitionRepositoryDataJpaTest {

    @Autowired
    private DefinitionRepository definitionRepository;

    @Autowired
    private WordRepository wordRepository;

    @Test
    void persistsDefinitionWithWordRelationship() {
        Word word = wordRepository.saveAndFlush(new Word("lexicon", "en"));
        Definition definition = new Definition();
        definition.setWord(word);
        definition.setDefinitionText("A vocabulary.");
        definition.setPartOfSpeech("noun");
        definition.setSourceApi("dictionary-api");
        definition.setCachedAt(LocalDateTime.of(2026, 5, 1, 11, 0));

        Definition saved = definitionRepository.saveAndFlush(definition);
        Definition actual = definitionRepository.findById(saved.getId()).orElseThrow();

        assertEquals("A vocabulary.", actual.getDefinitionText());
        assertEquals("lexicon", actual.getWord().getText());
        assertTrue(actual.getId() > 0);
    }
}
