package com.ronanos.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebConfigTest {

    @Test
    void configuresCorsForReactDevelopmentServerWithCredentials() {
        CapturingCorsRegistry registry = new CapturingCorsRegistry();

        new WebConfig().addCorsMappings(registry);

        assertEquals("/api/**", registry.registration.pathPattern);
        assertEquals(List.of("http://localhost:5173"), registry.registration.allowedOrigins);
        assertEquals(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"), registry.registration.allowedMethods);
        assertEquals(List.of("*"), registry.registration.allowedHeaders);
        assertEquals(Boolean.TRUE, registry.registration.allowCredentials);
    }

    private static class CapturingCorsRegistry extends CorsRegistry {
        private CapturingCorsRegistration registration;

        @Override
        public CorsRegistration addMapping(String pathPattern) {
            registration = new CapturingCorsRegistration(pathPattern);
            return registration;
        }
    }

    private static class CapturingCorsRegistration extends CorsRegistration {
        private final String pathPattern;
        private List<String> allowedOrigins = new ArrayList<>();
        private List<String> allowedMethods = new ArrayList<>();
        private List<String> allowedHeaders = new ArrayList<>();
        private Boolean allowCredentials;

        CapturingCorsRegistration(String pathPattern) {
            super(pathPattern);
            this.pathPattern = pathPattern;
        }

        @Override
        public CorsRegistration allowedOrigins(String... origins) {
            allowedOrigins = List.of(origins);
            return this;
        }

        @Override
        public CorsRegistration allowedMethods(String... methods) {
            allowedMethods = List.of(methods);
            return this;
        }

        @Override
        public CorsRegistration allowedHeaders(String... headers) {
            allowedHeaders = List.of(headers);
            return this;
        }

        @Override
        public CorsRegistration allowCredentials(boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
            return this;
        }
    }
}
