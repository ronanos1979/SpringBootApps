package com.ronanos.backend.user;

import com.ronanos.backend.security.SecurityConfiguration;
import com.ronanos.backend.user.domain.User;
import com.ronanos.backend.user.persistence.UserRepository;
import com.ronanos.backend.user.web.UserController;
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
@Import(SecurityConfiguration.class)
class UserControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    // Required so Spring Security can find a UserDetailsService in the web layer context
    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listUsersReturnsJsonForAuthenticatedUser() throws Exception {
        User u = new User("ronan", "secret123", "r@r.com", "Ronan", "O");
        u.setId(1L);
        when(userRepository.findAll()).thenReturn(List.of(u));

        mockMvc.perform(get("/api/users").with(user("ronan").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].username").value("ronan"))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void createUserReturns201WithCreatedUser() throws Exception {
        User input = new User("newuser", "password123", "new@new.com", "New", "User");
        User saved = new User("newuser", "encoded", "new@new.com", "New", "User");
        saved.setId(2L);
        User currentUser = new User();
        currentUser.setId(1L);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser));
        when(userRepository.save(any())).thenReturn(saved);

        mockMvc.perform(post("/api/users")
                        .with(user("ronan").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "newuser",
                                  "password": "password123",
                                  "email": "new@new.com",
                                  "firstName": "New",
                                  "lastName": "User"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void updateUserReturns200WithUpdatedUser() throws Exception {
        User existing = new User("ronan", "old-encoded", "r@r.com", "Ronan", "O");
        existing.setId(1L);
        User update = new User("ronan", "newpassword1", "r@r.com", "Ronan", "O");
        User currentUser = new User();
        currentUser.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newpassword1")).thenReturn("new-encoded");
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/api/users/1")
                        .with(user("ronan").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "ronan",
                                  "password": "newpassword1",
                                  "email": "r@r.com",
                                  "firstName": "Ronan",
                                  "lastName": "O"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ronan"));
    }

    @Test
    void deleteUserReturns204() throws Exception {
        mockMvc.perform(delete("/api/users/1")
                        .with(user("ronan").roles("USER")))
                .andExpect(status().isNoContent());

        verify(userRepository).deleteById(1L);
    }

    @Test
    void updateUserReturns404WhenNotFound() throws Exception {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/99")
                        .with(user("ronan").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }
}
