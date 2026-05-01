package com.ronanos.lexiconlair.user;

import com.ronanos.lexiconlair.security.SpringSecurityConfiguration;
import com.ronanos.lexiconlair.user.domain.User;
import com.ronanos.lexiconlair.user.persistence.UserRepository;
import com.ronanos.lexiconlair.user.web.UserController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;


@WebMvcTest(UserController.class)
@Import(SpringSecurityConfiguration.class)
class UserControllerJPAMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    void listUsersReturnsRepositoryResultsForAuthenticatedUser() throws Exception {
        List<User> users = List.of(new User(), new User());
        when(userRepository.findAll()).thenReturn(users);

        mockMvc.perform(get("/list-users").with(user("ronan").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("user/listUsers"))
                .andExpect(model().attribute("users", users));
    }

    @Test
    void addUserGetReturnsFormView() throws Exception {
        mockMvc.perform(get("/add-user").with(user("ronan").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("user/addUser"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void addUserPostRedirectsWhenPayloadIsAccepted() throws Exception {
        User currentUser = new User();
        currentUser.setId(10L);
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser));
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");

        mockMvc.perform(post("/add-user")
                        .with(csrf())
                        .with(user("ronan").roles("USER"))
                        .param("username", "ronan")
                        .param("password", "password")
                        .param("email", "ronan@example.com")
                        .param("firstName", "Ronan")
                        .param("lastName", "Os"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("list-users"));

        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUserGetLoadsExistingUser() throws Exception {
        User existingUser = new User();
        existingUser.setUsername("existing-user");
        when(userRepository.findById(21L)).thenReturn(Optional.of(existingUser));

        mockMvc.perform(get("/update-user").with(user("ronan").roles("USER")).param("id", "21"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/addUser"))
                .andExpect(model().attribute("user", existingUser));
    }

    @Test
    void deleteUserRedirectsAfterDeletingEntity() throws Exception {
        mockMvc.perform(post("/delete-user")
                        .with(csrf())
                        .with(user("ronan").roles("USER"))
                        .param("id", "21"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("list-users"));

        verify(userRepository).deleteById(21L);
    }

    @Test
    void updateUserPostRedirectsWhenPayloadIsAccepted() throws Exception {
        User existingUser = new User();
        existingUser.setId(21L);
        existingUser.setPassword("existing-encoded-password");
        User currentUser = new User();
        currentUser.setId(10L);
        when(userRepository.findById(21L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser));
        when(passwordEncoder.encode("new-password")).thenReturn("new-encoded-password");

        mockMvc.perform(post("/update-user")
                        .with(csrf())
                        .with(user("ronan").roles("USER"))
                        .param("id", "21")
                        .param("username", "ronan")
                        .param("password", "new-password")
                        .param("email", "ronan@example.com")
                        .param("firstName", "Ronan")
                        .param("lastName", "Os"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("list-users"));

        verify(userRepository).save(any(User.class));
    }

    @Test
    void unauthenticatedRequestRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/list-users"))
                .andExpect(status().is3xxRedirection());

        verifyNoInteractions(userRepository);
    }
}
