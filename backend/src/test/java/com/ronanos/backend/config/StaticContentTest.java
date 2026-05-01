package com.ronanos.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StaticContentTest {

    @Test
    void indexPageContainsApiLandingContent() throws Exception {
        ClassPathResource index = new ClassPathResource("static/index.html");

        String html = index.getContentAsString(StandardCharsets.UTF_8);

        assertTrue(html.contains("Backend API"));
        assertTrue(html.contains("POST /api/auth/login"));
        assertTrue(html.contains("GET /api/users"));
    }
}
