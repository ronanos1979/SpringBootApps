package com.ronanos.backend.security;

import com.ronanos.backend.security.SecurityConfigurationMockMvcTest.TestController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestController.class)
@Import(SecurityConfiguration.class)
class SecurityConfigurationMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @org.springframework.boot.test.context.TestConfiguration
    static class TestSecurityBeans {
        @org.springframework.context.annotation.Bean
        UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
            return new InMemoryUserDetailsManager(User.builder()
                    .username("admin")
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
    }

    @Test
    void publicRoutesDoNotRequireAuthentication() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.html"));
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
                        .with(csrf())
                        .param("username", "admin")
                        .param("password", "change-me"))
                .andExpect(status().isOk())
                .andExpect(request().sessionAttribute("SPRING_SECURITY_CONTEXT", notNullValue()));
    }

    @Test
    void formLoginRejectsInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .param("username", "admin")
                        .param("password", "wrong"))
                .andExpect(status().isUnauthorized())
                .andExpect(status().reason("Bad credentials"));
    }
}
