package com.ronanos.lexiconlair.bookword.dto;

import com.ronanos.lexiconlair.bookword.domain.BookWord;
import com.ronanos.lexiconlair.definition.domain.Definition;
import com.ronanos.lexiconlair.word.dto.WordResponse;

import java.time.LocalDateTime;
import java.util.List;

public record BookWordResponse(
        Long id,
        Long bookId,
        WordResponse word,
        List<DefinitionSummary> definitions,
        LocalDateTime createdAt,
        Long createdBy
) {
    public static BookWordResponse from(BookWord bookWord, List<Definition> definitions) {
        return new BookWordResponse(
                bookWord.getId(),
                bookWord.getBook().getId(),
                WordResponse.from(bookWord.getWord()),
                definitions.stream().map(DefinitionSummary::from).toList(),
                bookWord.getCreatedAt(),
                bookWord.getCreatedBy());
    }
}
