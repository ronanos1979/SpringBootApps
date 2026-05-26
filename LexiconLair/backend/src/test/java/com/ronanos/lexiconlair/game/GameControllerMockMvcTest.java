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
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/game/question"))
                .andExpect(status().isUnauthorized());
    }

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
                .andExpect(jsonPath("$.mode").value("easy"))
                .andExpect(jsonPath("$.correctDefinitionId").value(11))
                .andExpect(jsonPath("$.options[0].definitionText").value("Enduring pain without complaint."));
    }

    @Test
    void defaultModeIsEasy() throws Exception {
        User current = new User();
        current.setId(7L);
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(current));
        when(gameService.nextQuestion("easy", 7L)).thenReturn(new GameQuestionResponse(
                "easy", 1L, "stoic", "en", 11L,
                List.of(new GameOptionResponse(11L, "Enduring pain.", "adjective"))));

        mockMvc.perform(get("/api/game/question").with(user("ronan").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("easy"));
    }

    @Test
    void difficultModeIsForwarded() throws Exception {
        User current = new User();
        current.setId(7L);
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(current));
        when(gameService.nextQuestion("difficult", 7L)).thenReturn(new GameQuestionResponse(
                "difficult", 2L, "ephemeral", "en", 22L,
                List.of(new GameOptionResponse(22L, "Lasting a very short time.", "adjective"))));

        mockMvc.perform(get("/api/game/question?mode=difficult").with(user("ronan").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("difficult"))
                .andExpect(jsonPath("$.wordText").value("ephemeral"));
    }

    @Test
    void noWordsAvailableReturns404() throws Exception {
        User current = new User();
        current.setId(7L);
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(current));
        when(gameService.nextQuestion(any(), anyLong()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "No playable words available"));

        mockMvc.perform(get("/api/game/question?mode=easy").with(user("ronan").roles("USER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No playable words available"));
    }

    @Test
    void unsupportedModeReturns400() throws Exception {
        User current = new User();
        current.setId(7L);
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(current));
        when(gameService.nextQuestion(any(), anyLong()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported game mode"));

        mockMvc.perform(get("/api/game/question?mode=expert").with(user("ronan").roles("USER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsupported game mode"));
    }

    @Test
    void gameEndpointAccessibleByUserRole() throws Exception {
        User current = new User();
        current.setId(3L);
        when(userRepository.findByUsername("regularuser")).thenReturn(Optional.of(current));
        when(gameService.nextQuestion("easy", 3L)).thenReturn(new GameQuestionResponse(
                "easy", 5L, "stoic", "en", 15L,
                List.of(new GameOptionResponse(15L, "Enduring pain.", "adjective"))));

        mockMvc.perform(get("/api/game/question?mode=easy").with(user("regularuser").roles("USER")))
                .andExpect(status().isOk());
    }
}
