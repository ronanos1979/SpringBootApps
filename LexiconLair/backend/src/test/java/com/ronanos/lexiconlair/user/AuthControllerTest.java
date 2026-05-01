package com.ronanos.lexiconlair.user;

import com.ronanos.lexiconlair.user.domain.User;
import com.ronanos.lexiconlair.user.dto.UserResponse;
import com.ronanos.lexiconlair.user.persistence.UserRepository;
import com.ronanos.lexiconlair.user.web.AuthController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void currentUserReturnsAuthenticatedUser() {
        User user = new User("admin", "encoded", "admin@example.com", "Admin", "User");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        AuthController controller = new AuthController(userRepository);

        ResponseEntity<UserResponse> response = controller.currentUser(new TestingAuthenticationToken("admin", "password"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("admin", response.getBody().username());
        assertEquals("admin@example.com", response.getBody().email());
    }

    @Test
    void currentUserReturns404WhenAuthenticatedUserIsNotFound() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());
        AuthController controller = new AuthController(userRepository);

        ResponseEntity<UserResponse> response = controller.currentUser(new TestingAuthenticationToken("missing", "password"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
