package com.ronanos.lexiconlair.bookword.dto;

import com.ronanos.lexiconlair.bookword.domain.BookWord;
import com.ronanos.lexiconlair.definition.domain.Definition;
import com.ronanos.lexiconlair.word.domain.Word;
import com.ronanos.lexiconlair.word.dto.WordResponse;

import java.time.LocalDateTime;
import java.util.List;

public record WordSearchResult(
        Long bookWordId,
        Long bookId,
        String bookTitle,
        String authorDisplayName,
        WordResponse word,
        List<DefinitionSummary> definitions,
        LocalDateTime createdAt
) {
    public static WordSearchResult from(BookWord bookWord, List<Definition> definitions) {
        return new WordSearchResult(
                bookWord.getId(),
                bookWord.getBook().getId(),
                bookWord.getBook().getTitle(),
                bookWord.getBook().getAuthor().getDisplayName(),
                WordResponse.from(bookWord.getWord()),
                definitions.stream().map(DefinitionSummary::from).toList(),
                bookWord.getCreatedAt());
    }

    public static WordSearchResult from(Word word, List<Definition> definitions) {
        return new WordSearchResult(
                null,
                null,
                null,
                null,
                WordResponse.from(word),
                definitions.stream().map(DefinitionSummary::from).toList(),
                word.getCreatedAt());
    }
}
