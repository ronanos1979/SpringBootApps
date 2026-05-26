package com.ronanos.lexiconlair.admin;

import com.ronanos.lexiconlair.admin.domain.AdminSettings;
import com.ronanos.lexiconlair.admin.service.AdminSettingsService;
import com.ronanos.lexiconlair.admin.service.ExternalApiThrottleService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExternalApiThrottleServiceTest {

    @Test
    void readsSettingsForEachExternalApiCall() {
        AdminSettingsService adminSettingsService = mock(AdminSettingsService.class);
        when(adminSettingsService.getSettings()).thenReturn(new AdminSettings(1L, 0, 10));
        ExternalApiThrottleService service = new ExternalApiThrottleService(adminSettingsService);

        service.beforeExternalApiCall();
        service.beforeExternalApiCall();

        verify(adminSettingsService, times(2)).getSettings();
    }

    @Test
    void noDelayWhenDelayMsIsZero() {
        AdminSettingsService adminSettingsService = mock(AdminSettingsService.class);
        when(adminSettingsService.getSettings()).thenReturn(new AdminSettings(1L, 0, 1));
        ExternalApiThrottleService service = new ExternalApiThrottleService(adminSettingsService);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            service.beforeExternalApiCall();
        }
        long elapsed = System.currentTimeMillis() - start;

        org.junit.jupiter.api.Assertions.assertTrue(elapsed < 1000,
                "No delay expected when delayMs=0, but took " + elapsed + "ms");
    }

    @Test
    void noDelayOnFirstCall() {
        AdminSettingsService adminSettingsService = mock(AdminSettingsService.class);
        when(adminSettingsService.getSettings()).thenReturn(new AdminSettings(1L, 5000, 1));
        ExternalApiThrottleService service = new ExternalApiThrottleService(adminSettingsService);

        long start = System.currentTimeMillis();
        service.beforeExternalApiCall();
        long elapsed = System.currentTimeMillis() - start;

        org.junit.jupiter.api.Assertions.assertTrue(elapsed < 1000,
                "First call should never sleep, but took " + elapsed + "ms");
    }

    @Test
    void settingsAreReadBeforeEachCall() {
        AdminSettingsService adminSettingsService = mock(AdminSettingsService.class);
        when(adminSettingsService.getSettings()).thenReturn(new AdminSettings(1L, 0, 10));
        ExternalApiThrottleService service = new ExternalApiThrottleService(adminSettingsService);

        service.beforeExternalApiCall();
        service.beforeExternalApiCall();
        service.beforeExternalApiCall();

        verify(adminSettingsService, atLeast(3)).getSettings();
    }
}
