package com.ronanos.lexiconlair.book.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BookForm {
    @NotBlank
    private String title;

    @NotNull
    private Long authorId;

    public BookForm() {};

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    @Override
    public String toString() {
        return "BookForm{" +
                "title='" + title + '\'' +
                ", authorId=" + authorId +
                '}';
    }
}
