package com.ronanos.lexiconlair.book.dto;

import com.ronanos.lexiconlair.author.dto.AuthorResponse;
import com.ronanos.lexiconlair.book.domain.Book;

import java.time.LocalDateTime;

public record BookResponse(
        Long id,
        String title,
        AuthorResponse author,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy
) {
    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                AuthorResponse.from(book.getAuthor()),
                book.getCreatedAt(),
                book.getUpdatedAt(),
                book.getCreatedBy(),
                book.getUpdatedBy());
    }
}
