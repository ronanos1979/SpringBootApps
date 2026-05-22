package com.ronanos.lexiconlair.bookword.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddWordToBookRequest(
        @NotBlank String text,
        @NotBlank @Size(max = 20) String language
) {}
