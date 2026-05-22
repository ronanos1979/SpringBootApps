package com.ronanos.lexiconlair.admin.dto;

import jakarta.validation.constraints.Min;

public record AdminSettingsRequest(
        @Min(0) int externalApiDelayMs,
        @Min(1) int externalApiBatchSize,
        @Min(2) int gameOptionCount,
        @Min(1) int gameQuestionCount
) {
}
