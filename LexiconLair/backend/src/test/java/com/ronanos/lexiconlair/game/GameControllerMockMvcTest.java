package com.ronanos.lexiconlair.game;

import com.ronanos.lexiconlair.game.dto.GameOptionResponse;
import com.ronanos.lexiconlair.game.dto.GameQuestionResponse;
import com.ronanos.lexiconlair.game.service.GameService;
import com.ronanos.lexiconlair.game.web.GameController;
import com.ronanos.lexiconlair.security.SpringSecurityConfiguration;
import com.ronanos.lexiconlair.user.domain.User;
import com.ronanos.lexiconlair.user.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GameController.class)
@Import(SpringSecurityConfiguration.class)
class GameControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameService gameService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void returnsQuestionForModeAndCurrentUser() throws Exception {
        User current = new User();
        current.setId(7L);
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(current));
        when(gameService.nextQuestion("easy", 7L)).thenReturn(new GameQuestionResponse(
                "easy",
                1L,
                "stoic",
                "en",
                11L,
                List.of(new GameOptionResponse(11L, "Enduring pain without complaint.", "adjective"))));

        mockMvc.perform(get("/api/game/question?mode=easy").with(user("ronan").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wordText").value("stoic"))
                .andExpect(jsonPath("$.correctDefinitionId").value(11))
                .andExpect(jsonPath("$.options[0].definitionText").value("Enduring pain without complaint."));
    }
}
