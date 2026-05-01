package com.ronanos.lexiconlair.author.dto;

import com.ronanos.lexiconlair.author.domain.Author;

public record AuthorResponse(
        Long id,
        String firstName,
        String lastName,
        String displayName
) {
    public static AuthorResponse from(Author author) {
        return new AuthorResponse(
                author.getId(),
                author.getFirstName(),
                author.getLastName(),
                author.getDisplayName());
    }
}
