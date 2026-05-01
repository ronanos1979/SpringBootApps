package com.ronanos.lexiconlair.dictionary.mapper;

import com.ronanos.lexiconlair.definition.domain.Definition;
import com.ronanos.lexiconlair.dictionary.dto.DictionaryWordDTO;
import com.ronanos.lexiconlair.word.domain.Word;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DefinitionMapper {

    public List<Definition> mapToDefinitions(Word word, DictionaryWordDTO entry) {
        List<Definition> results = new ArrayList<>();

        if (entry == null || entry.getMeanings() == null) {
            return results;
        }

        for (DictionaryWordDTO.MeaningDTO meaning : entry.getMeanings()) {
            if (meaning == null || meaning.getDefinitions() == null) {
                continue;
            }

            for (DictionaryWordDTO.MeaningDTO.DefinitionDTO def : meaning.getDefinitions()) {
                if (def == null || def.getDefinition() == null || def.getDefinition().isBlank()) {
                    continue;
                }

                Definition definition = new Definition();
                definition.setWord(word);
                definition.setPartOfSpeech(meaning.getPartOfSpeech());
                definition.setDefinitionText(def.getDefinition());
                definition.setExample(def.getExample());
                definition.setSourceApi("dictionaryapi.dev");
                definition.setCachedAt(LocalDateTime.now());
                results.add(definition);
            }
        }

        return results;
    }
}
