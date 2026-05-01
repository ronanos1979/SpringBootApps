package com.ronanos.backend.user;

import com.ronanos.backend.user.domain.User;
import com.ronanos.backend.user.persistence.UserRepository;
import com.ronanos.backend.user.web.UserController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserController controller;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listUsersReturnsRepositoryResults() {
        List<User> users = List.of(new User(), new User());
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = controller.listUsers();

        assertSame(users, result);
    }

    @Test
    void createUserEncodesPasswordAndSetsAuditFields() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("ronan", "password"));
        User user = new User("newuser", "rawpassword", "e@e.com", "First", "Last");
        User currentUser = new User();
        currentUser.setId(42L);
        when(passwordEncoder.encode("rawpassword")).thenReturn("encoded");
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = controller.createUser(user);

        assertEquals("encoded", result.getPassword());
        assertEquals(42L, result.getCreatedBy());
        assertEquals(LocalDateTime.now().toLocalDate(), result.getCreatedAt().toLocalDate());
        verify(userRepository).save(user);
    }

    @Test
    void updateUserEncodesNewPassword() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("ronan", "password"));
        User existing = new User();
        existing.setId(5L);
        existing.setPassword("old-encoded");
        User update = new User("newname", "newpassword", "e@e.com", "F", "L");
        User currentUser = new User();
        currentUser.setId(42L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newpassword")).thenReturn("new-encoded");
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = controller.updateUser(5L, update);

        assertEquals("new-encoded", result.getPassword());
        assertEquals(42L, result.getUpdatedBy());
        assertEquals(LocalDateTime.now().toLocalDate(), result.getUpdatedAt().toLocalDate());
    }

    @Test
    void updateUserKeepsExistingPasswordWhenBlank() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("ronan", "password"));
        User existing = new User();
        existing.setId(5L);
        existing.setPassword("old-encoded");
        User update = new User("newname", "   ", "e@e.com", "F", "L");
        User currentUser = new User();
        currentUser.setId(42L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = controller.updateUser(5L, update);

        assertEquals("old-encoded", result.getPassword());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updateUserThrowsNotFoundWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.updateUser(99L, new User()));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void deleteUserCallsRepositoryDeleteById() {
        controller.deleteUser(17L);

        verify(userRepository).deleteById(17L);
    }
}
