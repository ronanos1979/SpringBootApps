package com.ronanos.lexiconlair.user;

import com.ronanos.lexiconlair.user.domain.User;
import com.ronanos.lexiconlair.user.persistence.UserRepository;
import com.ronanos.lexiconlair.user.web.UserController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
    void listAllUsersAddsRepositoryResultsToModel() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("ronan", "password"));
        ModelMap model = new ModelMap();
        List<User> users = List.of(new User(), new User());
        when(userRepository.findAll()).thenReturn(users);

        String viewName = controller.listAllUsers(model);

        assertEquals("user/listUsers", viewName);
        assertSame(users, model.get("users"));
    }

    @Test
    void showNewUserPageAddsBlankUserToModel() {
        ModelMap model = new ModelMap();

        String viewName = controller.showCreateUserForm(model);

        assertEquals("user/addUser", viewName);
        User user = (User) model.get("user");
        assertNull(user.getId());
        assertEquals("", user.getUsername());
        assertEquals("", user.getEmail());
        assertNull(user.getUpdatedBy());
    }

    @Test
    void addUserReturnsFormViewWhenValidationFails() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("ronan", "change-me"));
        ModelMap model = new ModelMap();
        User user = new User();
        BindingResult result = new BeanPropertyBindingResult(user, "user");
        result.reject("invalid");

        String viewName = controller.createUser(model, user, result);

        assertEquals("user/addUser", viewName);
        verify(userRepository, never()).save(user);
    }

    @Test
    void addUserSetsAuditFieldsSavesUserAndRedirects() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("ronan", "password"));
        ModelMap model = new ModelMap();
        User user = new User();
        user.setUsername("ronan");
        user.setPassword("password123");
        BindingResult result = new BeanPropertyBindingResult(user, "user");
        User currentUser = new User();
        currentUser.setId(42L);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser));

        String viewName = controller.createUser(model, user, result);

        assertEquals("redirect:list-users", viewName);
        verify(userRepository).save(user);
        assertEquals(42L, user.getUpdatedBy());
        assertEquals("encoded-password", user.getPassword());
        assertEquals(LocalDateTime.now().toLocalDate(), user.getLastUpdated().toLocalDate());
        assertSame(user, model.get("user"));
    }

    @Test
    void deleteUserDeletesByIdAndRedirects() {
        String viewName = controller.deleteUser(17L);

        assertEquals("redirect:list-users", viewName);
        verify(userRepository).deleteById(17L);
    }

    @Test
    void showUpdateUserPageLoadsExistingUser() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("ronan", "password"));
        ModelMap model = new ModelMap();
        User existing = new User();
        existing.setUsername("existing-user");
        when(userRepository.findById(8L)).thenReturn(Optional.of(existing));

        String viewName = controller.showUpdateUserForm(8L, model);

        assertEquals("user/addUser", viewName);
        User modelUser = (User) model.get("user");
        assertSame(existing, modelUser);
        assertEquals("", modelUser.getPassword());
    }

    @Test
    void updateUserRedirectsWithoutSavingWhenValidationFails() {
        ModelMap model = new ModelMap();
        User user = new User();
        BindingResult result = new BeanPropertyBindingResult(user, "user");
        result.reject("invalid");

        String viewName = controller.updateUser(model, user, result);

        assertEquals("user/addUser", viewName);
        verify(userRepository, never()).save(user);
    }

    @Test
    void updateUserSetsAuditFieldsSavesUserAndRedirects() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("ronan", "password"));
        ModelMap model = new ModelMap();
        User user = new User();
        user.setId(9L);
        user.setUsername("updated-user");
        user.setPassword("new-password");
        BindingResult result = new BeanPropertyBindingResult(user, "user");
        User existingUser = new User();
        User currentUser = new User();
        currentUser.setId(21L);
        when(userRepository.findById(9L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-password");
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser));

        String viewName = controller.updateUser(model, user, result);

        assertEquals("redirect:list-users", viewName);
        verify(userRepository).save(user);
        assertEquals(21L, user.getUpdatedBy());
        assertEquals("encoded-password", user.getPassword());
        assertEquals(LocalDateTime.now().toLocalDate(), user.getLastUpdated().toLocalDate());
    }

    @Test
    void updateUserKeepsExistingPasswordWhenSubmittedPasswordIsBlank() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("ronan", "password"));
        ModelMap model = new ModelMap();
        User user = new User();
        user.setId(9L);
        user.setUsername("updated-user");
        user.setPassword("   ");
        BindingResult result = new BeanPropertyBindingResult(user, "user");
        User existingUser = new User();
        existingUser.setPassword("existing-encoded-password");
        User currentUser = new User();
        currentUser.setId(21L);
        when(userRepository.findById(9L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser));

        String viewName = controller.updateUser(model, user, result);

        assertEquals("redirect:list-users", viewName);
        assertEquals("existing-encoded-password", user.getPassword());
        assertEquals(21L, user.getUpdatedBy());
        verify(userRepository).save(user);
    }
}
