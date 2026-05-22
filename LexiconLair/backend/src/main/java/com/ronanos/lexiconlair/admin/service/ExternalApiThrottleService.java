package com.ronanos.lexiconlair.admin.service;

import com.ronanos.lexiconlair.admin.domain.AdminSettings;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class ExternalApiThrottleService {

    private final AdminSettingsService adminSettingsService;
    private final AtomicLong callCount = new AtomicLong();

    public ExternalApiThrottleService(AdminSettingsService adminSettingsService) {
        this.adminSettingsService = adminSettingsService;
    }

    public void beforeExternalApiCall() {
        AdminSettings settings = adminSettingsService.getSettings();
        int delayMs = settings.getExternalApiDelayMs();
        int batchSize = Math.max(1, settings.getExternalApiBatchSize());
        long nextCall = callCount.incrementAndGet();

        if (delayMs <= 0 || nextCall <= 1 || (nextCall - 1) % batchSize != 0) {
            return;
        }

        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
