package com.ronanos.lexiconlair.admin;

import com.ronanos.lexiconlair.admin.domain.AdminSettings;
import com.ronanos.lexiconlair.admin.dto.AdminSettingsRequest;
import com.ronanos.lexiconlair.admin.service.AdminSettingsService;
import com.ronanos.lexiconlair.admin.web.AdminSettingsController;
import com.ronanos.lexiconlair.security.SpringSecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminSettingsController.class)
@Import(SpringSecurityConfiguration.class)
class AdminSettingsControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminSettingsService adminSettingsService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void getSettingsReturnsCurrentSettings() throws Exception {
        when(adminSettingsService.getSettings()).thenReturn(new AdminSettings(1L, 50, 10));

        mockMvc.perform(get("/api/admin/settings").with(user("ronan").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalApiDelayMs").value(50))
                .andExpect(jsonPath("$.externalApiBatchSize").value(10))
                .andExpect(jsonPath("$.gameOptionCount").value(4))
                .andExpect(jsonPath("$.gameQuestionCount").value(10));
    }

    @Test
    void updateSettingsReturnsSavedSettings() throws Exception {
        when(adminSettingsService.updateSettings(argThat((AdminSettingsRequest request) ->
                        request.externalApiDelayMs() == 100 &&
                        request.externalApiBatchSize() == 5 &&
                        request.gameOptionCount() == 6 &&
                        request.gameQuestionCount() == 12)))
                .thenReturn(new AdminSettings(1L, 100, 5, 6, 12));

        mockMvc.perform(put("/api/admin/settings")
                        .with(user("ronan").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "externalApiDelayMs": 100,
                                  "externalApiBatchSize": 5,
                                  "gameOptionCount": 6,
                                  "gameQuestionCount": 12
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalApiDelayMs").value(100))
                .andExpect(jsonPath("$.externalApiBatchSize").value(5))
                .andExpect(jsonPath("$.gameOptionCount").value(6))
                .andExpect(jsonPath("$.gameQuestionCount").value(12));
    }

    @Test
    void updateSettingsRejectsInvalidBatchSize() throws Exception {
        mockMvc.perform(put("/api/admin/settings")
                        .with(user("ronan").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "externalApiDelayMs": 0,
                                  "externalApiBatchSize": 0,
                                  "gameOptionCount": 4,
                                  "gameQuestionCount": 10
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
