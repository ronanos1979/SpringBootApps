package com.ronanos.lexiconlair.admin;

import com.ronanos.lexiconlair.admin.domain.AdminSettings;
import com.ronanos.lexiconlair.admin.dto.AdminSettingsRequest;
import com.ronanos.lexiconlair.admin.persistence.AdminSettingsRepository;
import com.ronanos.lexiconlair.admin.service.AdminSettingsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminSettingsServiceTest {

    @Mock
    private AdminSettingsRepository adminSettingsRepository;

    @InjectMocks
    private AdminSettingsService adminSettingsService;

    @Test
    void getSettingsReturnsExistingRecord() {
        AdminSettings existing = new AdminSettings(1L, 100, 20, 6, 15);
        when(adminSettingsRepository.findById(AdminSettingsService.SETTINGS_ID))
                .thenReturn(Optional.of(existing));

        AdminSettings result = adminSettingsService.getSettings();

        assertEquals(100, result.getExternalApiDelayMs());
        assertEquals(20, result.getExternalApiBatchSize());
        assertEquals(6, result.getGameOptionCount());
        assertEquals(15, result.getGameQuestionCount());
        verify(adminSettingsRepository, never()).save(any());
    }

    @Test
    void getSettingsCreatesDefaultRecordWhenNoneExists() {
        when(adminSettingsRepository.findById(AdminSettingsService.SETTINGS_ID))
                .thenReturn(Optional.empty());
        when(adminSettingsRepository.save(any(AdminSettings.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AdminSettings result = adminSettingsService.getSettings();

        assertEquals(50, result.getExternalApiDelayMs());
        assertEquals(10, result.getExternalApiBatchSize());
        assertEquals(4, result.getGameOptionCount());
        assertEquals(10, result.getGameQuestionCount());
        verify(adminSettingsRepository).save(argThat(s -> s.getId().equals(AdminSettingsService.SETTINGS_ID)));
    }

    @Test
    void updateSettingsAppliesAllFieldsFromRequest() {
        AdminSettings existing = new AdminSettings(1L, 50, 10, 4, 10);
        when(adminSettingsRepository.findById(AdminSettingsService.SETTINGS_ID))
                .thenReturn(Optional.of(existing));
        when(adminSettingsRepository.save(any(AdminSettings.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AdminSettingsRequest request = new AdminSettingsRequest(200, 25, 8, 20);
        AdminSettings result = adminSettingsService.updateSettings(request);

        assertEquals(200, result.getExternalApiDelayMs());
        assertEquals(25, result.getExternalApiBatchSize());
        assertEquals(8, result.getGameOptionCount());
        assertEquals(20, result.getGameQuestionCount());
    }

    @Test
    void updateSettingsSavesUpdatedRecord() {
        AdminSettings existing = new AdminSettings(1L, 50, 10, 4, 10);
        when(adminSettingsRepository.findById(AdminSettingsService.SETTINGS_ID))
                .thenReturn(Optional.of(existing));
        when(adminSettingsRepository.save(any(AdminSettings.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        adminSettingsService.updateSettings(new AdminSettingsRequest(0, 1, 2, 1));

        verify(adminSettingsRepository).save(argThat(s ->
                s.getExternalApiDelayMs() == 0
                        && s.getExternalApiBatchSize() == 1
                        && s.getGameOptionCount() == 2
                        && s.getGameQuestionCount() == 1));
    }
}
