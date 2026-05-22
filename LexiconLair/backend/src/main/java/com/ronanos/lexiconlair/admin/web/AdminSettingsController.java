package com.ronanos.lexiconlair.admin.web;

import com.ronanos.lexiconlair.admin.dto.AdminSettingsRequest;
import com.ronanos.lexiconlair.admin.dto.AdminSettingsResponse;
import com.ronanos.lexiconlair.admin.service.AdminSettingsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/settings")
public class AdminSettingsController {

    private final AdminSettingsService adminSettingsService;

    public AdminSettingsController(AdminSettingsService adminSettingsService) {
        this.adminSettingsService = adminSettingsService;
    }

    @GetMapping
    public AdminSettingsResponse getSettings() {
        return AdminSettingsResponse.from(adminSettingsService.getSettings());
    }

    @PutMapping
    public AdminSettingsResponse updateSettings(@Valid @RequestBody AdminSettingsRequest request) {
        return AdminSettingsResponse.from(adminSettingsService.updateSettings(request));
    }
}
