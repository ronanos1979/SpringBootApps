package com.ronanos.lexiconlair.word;

import com.ronanos.lexiconlair.security.SpringSecurityConfiguration;
import com.ronanos.lexiconlair.word.domain.Word;
import com.ronanos.lexiconlair.word.persistence.WordRepository;
import com.ronanos.lexiconlair.word.service.WordDefinitionService;
import com.ronanos.lexiconlair.word.web.WordController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WordController.class)
@Import(SpringSecurityConfiguration.class)
class WordControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WordRepository wordRepository;

    @MockitoBean
    private WordDefinitionService wordDefinitionService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void listWordsReturnsJsonForAuthenticatedUser() throws Exception {
        when(wordRepository.findAll()).thenReturn(List.of(new Word("lexicon", "en")));

        mockMvc.perform(get("/api/words").with(user("ronan").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text").value("lexicon"))
                .andExpect(jsonPath("$[0].language").value("en"));
    }

    @Test
    void getWordReturnsWordResponseDto() throws Exception {
        when(wordRepository.findById(1L)).thenReturn(Optional.of(new Word("serendipity", "en")));

        mockMvc.perform(get("/api/words/1").with(user("ronan").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("serendipity"))
                .andExpect(jsonPath("$.language").value("en"));
    }

    @Test
    void createWordDelegatesToDefinitionService() throws Exception {
        when(wordDefinitionService.saveWordWithDefinitions(any(Word.class)))
                .thenReturn(new Word("ephemeral", "en"));

        mockMvc.perform(post("/api/words")
                        .with(user("ronan").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "ephemeral",
                                  "language": "en"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("ephemeral"));

        verify(wordDefinitionService).saveWordWithDefinitions(argThat(word ->
                word.getText().equals("ephemeral") && word.getLanguage().equals("en")));
    }

    @Test
    void createWordRejectsShortText() throws Exception {
        mockMvc.perform(post("/api/words")
                        .with(user("ronan").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "hi",
                                  "language": "en"
                                }
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("text")))
                .andExpect(jsonPath("$.path").value("/api/words"));
    }

    @Test
    void updateWordReturnsUpdatedWord() throws Exception {
        Word existing = new Word("oldword", "en");
        when(wordRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(wordRepository.save(any(Word.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/api/words/1")
                        .with(user("ronan").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "newword",
                                  "language": "ga"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("newword"))
                .andExpect(jsonPath("$.language").value("ga"));
    }

    @Test
    void getWordReturns404WhenMissing() throws Exception {
        when(wordRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/words/99").with(user("ronan").roles("USER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Word not found"))
                .andExpect(jsonPath("$.path").value("/api/words/99"));
    }

    @Test
    void deleteWordReturns204() throws Exception {
        mockMvc.perform(delete("/api/words/1").with(user("ronan").roles("USER")))
                .andExpect(status().isNoContent());

        verify(wordRepository).deleteById(1L);
    }
}
