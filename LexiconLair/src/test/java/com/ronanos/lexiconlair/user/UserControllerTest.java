package com.ronanos.lexiconlair.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserRepository userRepository;

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

        assertEquals("listUsers", viewName);
        assertSame(users, model.get("users"));
    }

    @Test
    void showNewUserPageAddsBlankUserToModel() {
        ModelMap model = new ModelMap();

        String viewName = controller.showNewUserPage(model);

        assertEquals("addUser", viewName);
        User user = (User) model.get("user");
        assertNull(user.getId());
        assertEquals("", user.getUsername());
        assertEquals("", user.getEmail());
        assertNull(user.getUpdatedBy());
    }

    @Test
    void addUserReturnsFormViewWhenValidationFails() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("ronan", "password"));
        ModelMap model = new ModelMap();
        User user = new User();
        BindingResult result = new BeanPropertyBindingResult(user, "user");
        result.reject("invalid");

        String viewName = controller.addUser(model, user, result);

        assertEquals("addUser", viewName);
        verify(userRepository, never()).save(user);
    }

    @Test
    void addUserSetsAuditFieldsSavesUserAndRedirects() {
        ModelMap model = new ModelMap();
        User user = new User();
        user.setUsername("ronan");
        BindingResult result = new BeanPropertyBindingResult(user, "user");

        String viewName = controller.addUser(model, user, result);

        assertEquals("redirect:list-users", viewName);
        verify(userRepository).save(user);
        assertEquals(0L, user.getUpdatedBy());
        assertEquals(LocalDateTime.now().toLocalDate(), user.getLastUpdated().toLocalDate());
        assertSame(user, model.get("user"));
    }

    @Test
    void deleteUserDeletesByIdAndRedirects() {
        String viewName = controller.deleteUser(17);

        assertEquals("redirect:list-users", viewName);
        verify(userRepository).deleteById(17);
    }

    @Test
    void showUpdateUserPageLoadsExistingUser() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("ronan", "password"));
        ModelMap model = new ModelMap();
        User existing = new User();
        existing.setUsername("existing-user");
        when(userRepository.findById(8)).thenReturn(Optional.of(existing));

        String viewName = controller.showUpdateUserPage(8, model);

        assertEquals("addUser", viewName);
        assertSame(existing, model.get("user"));
    }

    @Test
    void updateUserRedirectsWithoutSavingWhenValidationFails() {
        ModelMap model = new ModelMap();
        User user = new User();
        BindingResult result = new BeanPropertyBindingResult(user, "user");
        result.reject("invalid");

        String viewName = controller.updateUser(model, user, result);

        assertEquals("redirect:list-users", viewName);
        verify(userRepository, never()).save(user);
    }

    @Test
    void updateUserSetsAuditFieldsSavesUserAndRedirects() {
        ModelMap model = new ModelMap();
        User user = new User();
        user.setUsername("updated-user");
        BindingResult result = new BeanPropertyBindingResult(user, "user");

        String viewName = controller.updateUser(model, user, result);

        assertEquals("redirect:list-users", viewName);
        verify(userRepository).save(user);
        assertEquals(0L, user.getUpdatedBy());
        assertEquals(LocalDateTime.now().toLocalDate(), user.getLastUpdated().toLocalDate());
    }
}
