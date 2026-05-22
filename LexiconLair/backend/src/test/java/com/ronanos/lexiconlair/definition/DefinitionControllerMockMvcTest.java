package com.ronanos.lexiconlair.definition;

import com.ronanos.lexiconlair.definition.domain.Definition;
import com.ronanos.lexiconlair.definition.persistence.DefinitionRepository;
import com.ronanos.lexiconlair.definition.web.DefinitionController;
import com.ronanos.lexiconlair.security.SpringSecurityConfiguration;
import com.ronanos.lexiconlair.user.domain.User;
import com.ronanos.lexiconlair.user.persistence.UserRepository;
import com.ronanos.lexiconlair.word.domain.Word;
import com.ronanos.lexiconlair.word.persistence.WordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
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

@WebMvcTest(DefinitionController.class)
@Import(SpringSecurityConfiguration.class)
class DefinitionControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DefinitionRepository definitionRepository;

    @MockitoBean
    private WordRepository wordRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private User currentUser() {
        User u = new User();
        u.setId(1L);
        return u;
    }

    @Test
    void listDefinitionsReturnsJsonForAuthenticatedUser() throws Exception {
        Definition definition = definition("lexicon");
        when(definitionRepository.findAll()).thenReturn(List.of(definition));

        mockMvc.perform(get("/api/definitions").with(user("ronan").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].definitionText").value("A vocabulary."))
                .andExpect(jsonPath("$[0].word.text").value("lexicon"))
                .andExpect(jsonPath("$[0].word.language").value("en"));
    }

    @Test
    void getDefinitionReturnsDefinitionResponseDto() throws Exception {
        when(definitionRepository.findById(1L)).thenReturn(Optional.of(definition("lexicon")));

        mockMvc.perform(get("/api/definitions/1").with(user("ronan").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.definitionText").value("A vocabulary."))
                .andExpect(jsonPath("$.partOfSpeech").value("noun"))
                .andExpect(jsonPath("$.word.text").value("lexicon"));
    }

    @Test
    void createDefinitionReturns201WhenWordExists() throws Exception {
        when(wordRepository.findById(1L)).thenReturn(Optional.of(new Word("lexicon", "en")));
        when(definitionRepository.save(any(Definition.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/definitions")
                        .with(user("ronan").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "wordId": 1,
                                  "definitionText": "A vocabulary.",
                                  "partOfSpeech": "noun",
                                  "example": "The lexicon is broad.",
                                  "sourceApi": "dictionary-api",
                                  "cachedAt": "2026-05-01T10:00:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.word.text").value("lexicon"))
                .andExpect(jsonPath("$.definitionText").value("A vocabulary."));

        verify(definitionRepository).save(argThat(d ->
                d.getWord().getText().equals("lexicon")
                        && d.getDefinitionText().equals("A vocabulary.")
                        && d.getPartOfSpeech().equals("noun")));
    }

    @Test
    void createDefinitionSetsCreatedAtAndCreatedBy() throws Exception {
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser()));
        when(wordRepository.findById(1L)).thenReturn(Optional.of(new Word("lexicon", "en")));
        when(definitionRepository.save(any(Definition.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/definitions")
                        .with(user("ronan").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "wordId": 1,
                                  "definitionText": "A vocabulary.",
                                  "partOfSpeech": "noun",
                                  "sourceApi": "dictionary-api"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.createdBy").value(1));

        verify(definitionRepository).save(argThat(d ->
                d.getCreatedAt() != null && Long.valueOf(1L).equals(d.getCreatedBy())));
    }

    @Test
    void createDefinitionReturns400WhenWordIsMissing() throws Exception {
        when(wordRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/definitions")
                        .with(user("ronan").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "wordId": 99,
                                  "definitionText": "Missing word.",
                                  "partOfSpeech": "noun",
                                  "sourceApi": "dictionary-api"
                                }
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid word id"));
    }

    @Test
    void updateDefinitionSetsUpdatedAtAndUpdatedBy() throws Exception {
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser()));
        Definition existing = definition("oldword");
        when(definitionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(wordRepository.findById(2L)).thenReturn(Optional.of(new Word("updated", "en")));
        when(definitionRepository.save(any(Definition.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/api/definitions/1")
                        .with(user("ronan").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "wordId": 2,
                                  "definitionText": "Updated definition.",
                                  "partOfSpeech": "verb",
                                  "sourceApi": "dictionary-api"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedBy").value(1));

        verify(definitionRepository).save(argThat(d ->
                d.getUpdatedAt() != null && Long.valueOf(1L).equals(d.getUpdatedBy())));
    }

    @Test
    void updateDefinitionReturns404WhenMissing() throws Exception {
        when(definitionRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/definitions/99")
                        .with(user("ronan").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "wordId": 1,
                                  "definitionText": "A vocabulary.",
                                  "partOfSpeech": "noun",
                                  "sourceApi": "dictionary-api"
                                }
                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteDefinitionReturns204() throws Exception {
        mockMvc.perform(delete("/api/definitions/1").with(user("ronan").roles("ADMIN")))
                .andExpect(status().isNoContent());

        verify(definitionRepository).deleteById(1L);
    }

    private Definition definition(String wordText) {
        Definition definition = new Definition();
        definition.setWord(new Word(wordText, "en"));
        definition.setDefinitionText("A vocabulary.");
        definition.setPartOfSpeech("noun");
        definition.setExample("The lexicon is broad.");
        definition.setSourceApi("dictionary-api");
        definition.setCachedAt(LocalDateTime.of(2026, 5, 1, 10, 0));
        return definition;
    }
}
