package com.ronanos.lexiconlair.admin.dto;

import com.ronanos.lexiconlair.admin.domain.AdminSettings;

public record AdminSettingsResponse(
        int externalApiDelayMs,
        int externalApiBatchSize,
        int gameOptionCount,
        int gameQuestionCount
) {
    public static AdminSettingsResponse from(AdminSettings settings) {
        return new AdminSettingsResponse(
                settings.getExternalApiDelayMs(),
                settings.getExternalApiBatchSize(),
                settings.getGameOptionCount(),
                settings.getGameQuestionCount());
    }
}
