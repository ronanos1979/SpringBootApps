package com.ronanos.lexiconlair.book.dto;

import com.ronanos.lexiconlair.author.dto.AuthorResponse;
import com.ronanos.lexiconlair.book.domain.Book;

public record BookResponse(
        Long id,
        String title,
        AuthorResponse author
) {
    public static BookResponse from(Book book) {
        return new BookResponse(book.getId(), book.getTitle(), AuthorResponse.from(book.getAuthor()));
    }
}
