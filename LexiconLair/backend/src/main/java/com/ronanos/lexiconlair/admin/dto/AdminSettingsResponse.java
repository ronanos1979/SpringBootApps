package com.ronanos.lexiconlair.admin.dto;

import com.ronanos.lexiconlair.admin.domain.AdminSettings;

public record AdminSettingsResponse(
        int externalApiDelayMs,
        int externalApiBatchSize
) {
    public static AdminSettingsResponse from(AdminSettings settings) {
        return new AdminSettingsResponse(settings.getExternalApiDelayMs(), settings.getExternalApiBatchSize());
    }
}
