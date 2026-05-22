package com.ronanos.lexiconlair.security;

import com.ronanos.lexiconlair.security.SecurityConfigurationMockMvcTest.TestController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestController.class)
@Import(SpringSecurityConfiguration.class)
class SecurityConfigurationMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.context.TestConfiguration
    static class TestSecurityBeans {
        @Bean
        UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
            return new InMemoryUserDetailsManager(User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("change-me"))
                    .roles("ADMIN")
                    .build(), User.builder()
                    .username("reader")
                    .password(passwordEncoder.encode("change-me"))
                    .roles("USER")
                    .build());
        }
    }

    @RestController
    static class TestController {
        @GetMapping(path = "/", produces = MediaType.TEXT_PLAIN_VALUE)
        String home() {
            return "home";
        }

        @GetMapping(path = "/api/protected", produces = MediaType.TEXT_PLAIN_VALUE)
        String protectedApi() {
            return "protected";
        }

        @GetMapping(path = "/api/users", produces = MediaType.TEXT_PLAIN_VALUE)
        String usersApi() {
            return "users";
        }

        @GetMapping(path = "/api/game/question", produces = MediaType.TEXT_PLAIN_VALUE)
        String gameApi() {
            return "game";
        }
    }

    @Test
    void publicRootDoesNotRequireAuthentication() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    void apiRoutesRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/protected"))
                .andExpect(status().isUnauthorized())
                .andExpect(status().reason("Unauthorized"));
    }

    @Test
    void formLoginCreatesSessionForValidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .param("username", "admin")
                        .param("password", "change-me"))
                .andExpect(status().isOk())
                .andExpect(request().sessionAttribute("SPRING_SECURITY_CONTEXT", notNullValue()));
    }

    @Test
    void regularUsersCannotAccessAdminCrudApis() throws Exception {
        mockMvc.perform(get("/api/users").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("reader").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void regularUsersAreAuthorizedForGameApis() throws Exception {
        mockMvc.perform(get("/api/game/question").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("reader").roles("USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void formLoginRejectsInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .param("username", "admin")
                        .param("password", "wrong"))
                .andExpect(status().isUnauthorized())
                .andExpect(status().reason("Bad credentials"));
    }
}
