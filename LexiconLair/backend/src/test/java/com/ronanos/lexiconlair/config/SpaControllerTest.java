package com.ronanos.lexiconlair.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpaControllerTest {

    @Test
    void forwardsBrowserRoutesToIndex() {
        assertTrue(matchesFallback("authors"));
        assertTrue(matchesFallback("definitions"));
        assertTrue(matchesFallback("words/update/1"));
    }

    @Test
    void doesNotForwardApiOrStaticIndexRoutes() {
        assertFalse(matchesFallback("api/authors"));
        assertFalse(matchesFallback("assets/app.js"));
        assertFalse(matchesFallback("favicon.svg"));
        assertFalse(matchesFallback("index.html"));
    }

    private boolean matchesFallback(String path) {
        String[] patterns = SpaController.class.getDeclaredMethods()[0]
                .getAnnotation(RequestMapping.class)
                .value();
        Pattern pathPattern = Pattern.compile("^(?!api|assets|favicon|index\\.html$).*");

        assertTrue(Arrays.stream(patterns).allMatch(pattern -> pattern.contains("index\\.html$")));
        return pathPattern.matcher(path).matches();
    }
}
