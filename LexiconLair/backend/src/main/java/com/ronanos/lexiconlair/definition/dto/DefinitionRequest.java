package com.ronanos.lexiconlair.definition.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record DefinitionRequest(
        @NotNull Long wordId,
        @NotBlank String definitionText,
        @NotBlank @Size(max = 50) String partOfSpeech,
        String example,
        @NotBlank @Size(max = 100) String sourceApi,
        LocalDateTime cachedAt
) {
}
