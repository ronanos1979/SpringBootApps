package com.ronanos.lexiconlair.word.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WordRequest(
        @NotBlank @Size(min = 3, max = 255, message = "Minimum length is 3 characters") String text,
        @NotBlank @Size(max = 20) String language
) {
}
