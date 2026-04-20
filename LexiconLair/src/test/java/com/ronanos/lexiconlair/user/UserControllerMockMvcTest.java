package com.ronanos.lexiconlair.user;

import com.ronanos.lexiconlair.security.SpringSecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
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

    @Test
    void listUsersReturnsRepositoryResultsForAuthenticatedUser() throws Exception {
        List<User> users = List.of(new User(), new User());
        when(userRepository.findAll()).thenReturn(users);

        mockMvc.perform(get("/list-users").with(user("ronan").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("listUsers"))
                .andExpect(model().attribute("users", users));
    }

    @Test
    void addUserGetReturnsFormView() throws Exception {
        mockMvc.perform(get("/add-user").with(user("ronan").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("addUser"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void addUserPostRedirectsWhenPayloadIsAccepted() throws Exception {
        mockMvc.perform(post("/add-user")
                        .with(user("ronan").roles("USER"))
                        .param("id", "0")
                        .param("username", "ronan")
                        .param("password", "password")
                        .param("email", "ronan@example.com")
                        .param("firstName", "Ronan")
                        .param("lastName", "Os"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("list-users"));

        verify(userRepository).save(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void updateUserGetLoadsExistingUser() throws Exception {
        User existingUser = new User();
        existingUser.setUsername("existing-user");
        when(userRepository.findById(21)).thenReturn(Optional.of(existingUser));

        mockMvc.perform(get("/update-user").with(user("ronan").roles("USER")).param("id", "21"))
                .andExpect(status().isOk())
                .andExpect(view().name("addUser"))
                .andExpect(model().attribute("user", existingUser));
    }

    @Test
    void deleteUserRedirectsAfterDeletingEntity() throws Exception {
        mockMvc.perform(get("/delete-user").with(user("ronan").roles("USER")).param("id", "21"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("list-users"));

        verify(userRepository).deleteById(21);
    }

    @Test
    void unauthenticatedRequestRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/list-users"))
                .andExpect(status().is3xxRedirection());

        verifyNoInteractions(userRepository);
    }
}
