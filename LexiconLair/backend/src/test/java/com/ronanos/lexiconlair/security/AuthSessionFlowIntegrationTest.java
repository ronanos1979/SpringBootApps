package com.ronanos.lexiconlair.security;

import com.ronanos.lexiconlair.user.domain.User;
import com.ronanos.lexiconlair.user.persistence.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthSessionFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.save(new User(
                "admin",
                passwordEncoder.encode("change-me"),
                "admin@example.com",
                "Admin",
                "User"));
    }

    @Test
    void loginSessionAllowsApiAccessUntilLogout() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .param("username", "admin")
                        .param("password", "change-me"))
                .andExpect(status().isOk())
                .andExpect(request().sessionAttribute("SPRING_SECURITY_CONTEXT", notNullValue()))
                .andReturn();

        HttpSession session = login.getRequest().getSession(false);

        mockMvc.perform(get("/api/auth/me").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"username\":\"admin\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("password"))));

        mockMvc.perform(post("/api/auth/logout").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/auth/me").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void staticIndexUsesBootstrapLookAndFeel() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("bootstrap@5.3.8")))
                .andExpect(content().string(containsString("LexiconLair API")))
                .andExpect(content().string(containsString("card")));
    }
}
