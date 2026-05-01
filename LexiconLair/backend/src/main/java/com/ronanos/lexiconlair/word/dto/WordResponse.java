package com.ronanos.lexiconlair.word.dto;

import com.ronanos.lexiconlair.word.domain.Word;

public record WordResponse(
        Long id,
        String text,
        String language
) {
    public static WordResponse from(Word word) {
        return new WordResponse(word.getId(), word.getText(), word.getLanguage());
    }
}
