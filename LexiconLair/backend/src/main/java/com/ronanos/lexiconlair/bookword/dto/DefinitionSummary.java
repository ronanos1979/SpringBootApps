package com.ronanos.lexiconlair.bookword.dto;

import com.ronanos.lexiconlair.definition.domain.Definition;

import java.time.LocalDateTime;

public record DefinitionSummary(
        Long id,
        String definitionText,
        String partOfSpeech,
        String example,
        String sourceApi,
        LocalDateTime cachedAt
) {
    public static DefinitionSummary from(Definition definition) {
        return new DefinitionSummary(
                definition.getId(),
                definition.getDefinitionText(),
                definition.getPartOfSpeech(),
                definition.getExample(),
                definition.getSourceApi(),
                definition.getCachedAt());
    }
}
