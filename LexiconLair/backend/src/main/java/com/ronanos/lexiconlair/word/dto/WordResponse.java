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
        Long updatedBy
) {
    public static WordResponse from(Word word) {
        return new WordResponse(
                word.getId(),
                word.getText(),
                word.getLanguage(),
                word.getCreatedAt(),
                word.getUpdatedAt(),
                word.getCreatedBy(),
                word.getUpdatedBy());
    }
}
