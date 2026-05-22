package com.ronanos.lexiconlair.admin.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "admin_settings")
public class AdminSettings {

    @Id
    private Long id;

    private int externalApiDelayMs;
    private int externalApiBatchSize;

    public AdminSettings() {
    }

    public AdminSettings(Long id, int externalApiDelayMs, int externalApiBatchSize) {
        this.id = id;
        this.externalApiDelayMs = externalApiDelayMs;
        this.externalApiBatchSize = externalApiBatchSize;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getExternalApiDelayMs() {
        return externalApiDelayMs;
    }

    public void setExternalApiDelayMs(int externalApiDelayMs) {
        this.externalApiDelayMs = externalApiDelayMs;
    }

    public int getExternalApiBatchSize() {
        return externalApiBatchSize;
    }

    public void setExternalApiBatchSize(int externalApiBatchSize) {
        this.externalApiBatchSize = externalApiBatchSize;
    }
}
