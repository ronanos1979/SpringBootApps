package com.ronanos.lexiconlair.login;

import com.ronanos.lexiconlair.login.web.WelcomeController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
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
        Authentication auth = new TestingAuthenticationToken("ronan", "password");
        ModelMap model = new ModelMap();

        String viewName = controller.gotoWelcomePage(model, auth);

        assertEquals("welcome", viewName);
    }

    @Test
    void gotoWelcomePageRedirectsWhenAuthenticationIsNull() {
        ModelMap model = new ModelMap();

        String viewName = controller.gotoWelcomePage(model, null);

        assertEquals("redirect:/login", viewName);
    }

    @Test
    void gotoWelcomePageRedirectsToLoginWhenAnonymous() {
        Authentication auth = new TestingAuthenticationToken("anonymousUser", "password");
        ModelMap model = new ModelMap();

        String viewName = controller.gotoWelcomePage(model, auth);

        assertEquals("redirect:/login", viewName);
    }
}
