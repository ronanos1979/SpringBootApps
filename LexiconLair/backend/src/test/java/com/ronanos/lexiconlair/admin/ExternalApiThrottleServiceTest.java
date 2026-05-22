package com.ronanos.lexiconlair.admin;

import com.ronanos.lexiconlair.admin.domain.AdminSettings;
import com.ronanos.lexiconlair.admin.service.AdminSettingsService;
import com.ronanos.lexiconlair.admin.service.ExternalApiThrottleService;
import org.junit.jupiter.api.Test;

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
}
