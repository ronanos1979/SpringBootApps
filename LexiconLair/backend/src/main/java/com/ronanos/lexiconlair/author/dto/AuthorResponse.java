package com.ronanos.lexiconlair.author.dto;

import com.ronanos.lexiconlair.author.domain.Author;

import java.time.LocalDateTime;

public record AuthorResponse(
        Long id,
        String firstName,
        String lastName,
        String displayName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy
) {
    public static AuthorResponse from(Author author) {
        return new AuthorResponse(
                author.getId(),
                author.getFirstName(),
                author.getLastName(),
                author.getDisplayName(),
                author.getCreatedAt(),
                author.getUpdatedAt(),
                author.getCreatedBy(),
                author.getUpdatedBy());
    }
}
