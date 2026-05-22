package com.ronanos.lexiconlair.admin.persistence;

import com.ronanos.lexiconlair.admin.domain.AdminSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminSettingsRepository extends JpaRepository<AdminSettings, Long> {
}
