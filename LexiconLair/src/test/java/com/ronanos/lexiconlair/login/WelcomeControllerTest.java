package com.ronanos.lexiconlair.login;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WelcomeControllerTest {

    private final WelcomeController controller = new WelcomeController();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void gotoWelcomePageAddsLoggedInUserNameAndReturnsWelcomeView() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("ronan", "password"));
        ModelMap model = new ModelMap();

        String viewName = controller.gotoWelcomePage(model);

        assertEquals("welcome", viewName);
        assertEquals("ronan", model.get("name"));
    }
}
