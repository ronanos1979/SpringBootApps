package com.ronanos.lexiconlair.login;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticationServiceTest {

    private final AuthenticationService authenticationService = new AuthenticationService();

    @Test
    void authenticationReturnsTrueForValidCredentialsIgnoringCase() {
        boolean authenticated = authenticationService.authentication("Ronan", "PASSWORD");

        assertTrue(authenticated);
    }

    @Test
    void authenticationReturnsFalseForInvalidUsername() {
        boolean authenticated = authenticationService.authentication("other-user", "password");

        assertFalse(authenticated);
    }

    @Test
    void authenticationReturnsFalseForInvalidPassword() {
        boolean authenticated = authenticationService.authentication("ronan", "wrong-password");

        assertFalse(authenticated);
    }
}
