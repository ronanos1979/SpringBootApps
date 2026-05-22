package com.ronanos.lexiconlair.admin.service;

import com.ronanos.lexiconlair.admin.domain.AdminSettings;
import com.ronanos.lexiconlair.admin.dto.AdminSettingsRequest;
import com.ronanos.lexiconlair.admin.persistence.AdminSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminSettingsService {

    public static final long SETTINGS_ID = 1L;
    private static final int DEFAULT_EXTERNAL_API_DELAY_MS = 50;
    private static final int DEFAULT_EXTERNAL_API_BATCH_SIZE = 10;
    private static final int DEFAULT_GAME_OPTION_COUNT = 4;
    private static final int DEFAULT_GAME_QUESTION_COUNT = 10;

    private final AdminSettingsRepository adminSettingsRepository;

    public AdminSettingsService(AdminSettingsRepository adminSettingsRepository) {
        this.adminSettingsRepository = adminSettingsRepository;
    }

    @Transactional
    public AdminSettings getSettings() {
        return adminSettingsRepository.findById(SETTINGS_ID)
                .orElseGet(() -> adminSettingsRepository.save(
                        new AdminSettings(
                                SETTINGS_ID,
                                DEFAULT_EXTERNAL_API_DELAY_MS,
                                DEFAULT_EXTERNAL_API_BATCH_SIZE,
                                DEFAULT_GAME_OPTION_COUNT,
                                DEFAULT_GAME_QUESTION_COUNT)));
    }

    @Transactional
    public AdminSettings updateSettings(AdminSettingsRequest request) {
        AdminSettings settings = getSettings();
        settings.setExternalApiDelayMs(request.externalApiDelayMs());
        settings.setExternalApiBatchSize(request.externalApiBatchSize());
        settings.setGameOptionCount(request.gameOptionCount());
        settings.setGameQuestionCount(request.gameQuestionCount());
        return adminSettingsRepository.save(settings);
    }
}
