package com.ronanos.lexiconlair.word.dto;

import com.ronanos.lexiconlair.word.domain.Word;

import java.time.LocalDateTime;

public record WordResponse(
        Long id,
        String text,
        String language,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy,
        String definitionLookupStatus,
        Integer definitionLookupHttpStatus,
        String definitionLookupMessage,
        LocalDateTime definitionLookupAt
) {
    public static WordResponse from(Word word) {
        return new WordResponse(
                word.getId(),
                word.getText(),
                word.getLanguage(),
                word.getCreatedAt(),
                word.getUpdatedAt(),
                word.getCreatedBy(),
                word.getUpdatedBy(),
                word.getDefinitionLookupStatus(),
                word.getDefinitionLookupHttpStatus(),
                word.getDefinitionLookupMessage(),
                word.getDefinitionLookupAt());
    }
}
