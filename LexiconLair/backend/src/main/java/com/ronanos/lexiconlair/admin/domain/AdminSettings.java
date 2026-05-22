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
    private int gameOptionCount;
    private int gameQuestionCount;

    public AdminSettings() {
    }

    public AdminSettings(Long id, int externalApiDelayMs, int externalApiBatchSize) {
        this(id, externalApiDelayMs, externalApiBatchSize, 4, 10);
    }

    public AdminSettings(Long id, int externalApiDelayMs, int externalApiBatchSize, int gameOptionCount) {
        this(id, externalApiDelayMs, externalApiBatchSize, gameOptionCount, 10);
    }

    public AdminSettings(Long id, int externalApiDelayMs, int externalApiBatchSize, int gameOptionCount, int gameQuestionCount) {
        this.id = id;
        this.externalApiDelayMs = externalApiDelayMs;
        this.externalApiBatchSize = externalApiBatchSize;
        this.gameOptionCount = gameOptionCount;
        this.gameQuestionCount = gameQuestionCount;
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

    public int getGameOptionCount() {
        return gameOptionCount;
    }

    public void setGameOptionCount(int gameOptionCount) {
        this.gameOptionCount = gameOptionCount;
    }

    public int getGameQuestionCount() {
        return gameQuestionCount;
    }

    public void setGameQuestionCount(int gameQuestionCount) {
        this.gameQuestionCount = gameQuestionCount;
    }
}
