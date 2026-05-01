package com.ronanos.lexiconlair.definition.dto;

import com.ronanos.lexiconlair.definition.domain.Definition;
import com.ronanos.lexiconlair.word.dto.WordResponse;

import java.time.LocalDateTime;

public record DefinitionResponse(
        Long id,
        WordResponse word,
        String definitionText,
        String partOfSpeech,
        String example,
        String sourceApi,
        LocalDateTime cachedAt
) {
    public static DefinitionResponse from(Definition definition) {
        return new DefinitionResponse(
                definition.getId(),
                WordResponse.from(definition.getWord()),
                definition.getDefinitionText(),
                definition.getPartOfSpeech(),
                definition.getExample(),
                definition.getSourceApi(),
                definition.getCachedAt());
    }
}
