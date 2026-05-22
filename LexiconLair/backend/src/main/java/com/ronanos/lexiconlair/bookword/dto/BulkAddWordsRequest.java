package com.ronanos.lexiconlair.bookword.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BulkAddWordsRequest(
        @NotNull @Size(min = 1, max = 500) List<@NotBlank String> words,
        @NotBlank @Size(max = 20) String language
) {}
