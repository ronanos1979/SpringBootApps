package com.ronanos.lexiconlair.user;

import com.ronanos.lexiconlair.security.SpringSecurityConfiguration;
import com.ronanos.lexiconlair.user.domain.User;
import com.ronanos.lexiconlair.user.persistence.UserRepository;
import com.ronanos.lexiconlair.user.web.UserController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SpringSecurityConfiguration.class)
class UserControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listUsersReturnsUserResponseDtosWithoutPasswords() throws Exception {
        User user = new User("ronan", "encoded-password", "ronan@example.com", "Ronan", "O");
        user.setId(1L);
        when(userRepository.findAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users").with(user("ronan").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("ronan"))
                .andExpect(jsonPath("$[0].email").value("ronan@example.com"))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void getUserReturnsUserResponseDtoWithoutPassword() throws Exception {
        User existing = new User("ronan", "encoded-password", "ronan@example.com", "Ronan", "O");
        existing.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));

        mockMvc.perform(get("/api/users/1").with(user("ronan").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("ronan"))
                .andExpect(jsonPath("$.email").value("ronan@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void createUserEncodesPasswordAndReturns201() throws Exception {
        User currentUser = new User();
        currentUser.setId(7L);
        User saved = new User("newuser", "encoded", "new@example.com", "New", "User");
        saved.setId(2L);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser));
        when(userRepository.save(any(User.class))).thenReturn(saved);

        mockMvc.perform(post("/api/users")
                        .with(user("ronan").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "newuser",
                                  "password": "password123",
                                  "email": "new@example.com",
                                  "firstName": "New",
                                  "lastName": "User"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("newuser")
                        && user.getPassword().equals("encoded")
                        && user.getCreatedBy().equals(7L)));
    }

    @Test
    void createUserRejectsBlankPassword() throws Exception {
        mockMvc.perform(post("/api/users")
                        .with(user("ronan").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "newuser",
                                  "password": "",
                                  "email": "new@example.com",
                                  "firstName": "New",
                                  "lastName": "User"
                                }
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("password")))
                .andExpect(jsonPath("$.path").value("/api/users"));
    }

    @Test
    void updateUserKeepsExistingPasswordWhenBlank() throws Exception {
        User existing = new User("ronan", "old-encoded", "ronan@example.com", "Ronan", "O");
        existing.setId(1L);
        User currentUser = new User();
        currentUser.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/api/users/1")
                        .with(user("ronan").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "ronan",
                                  "password": "",
                                  "email": "ronan@example.com",
                                  "firstName": "Updated",
                                  "lastName": "User"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("old-encoded")
                        && user.getUpdatedBy().equals(1L)));
    }

    @Test
    void updateUserReturns404WhenMissing() throws Exception {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/99")
                        .with(user("ronan").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "missing",
                                  "password": "",
                                  "email": "missing@example.com",
                                  "firstName": "Missing",
                                  "lastName": "User"
                                }
                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found"))
                .andExpect(jsonPath("$.path").value("/api/users/99"));
    }

    @Test
    void deleteUserReturns204() throws Exception {
        mockMvc.perform(delete("/api/users/1").with(user("ronan").roles("USER")))
                .andExpect(status().isNoContent());

        verify(userRepository).deleteById(1L);
    }
}
