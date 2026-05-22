package com.ronanos.lexiconlair.word;

import com.ronanos.lexiconlair.bookword.domain.BookWord;
import com.ronanos.lexiconlair.bookword.persistence.BookWordRepository;
import com.ronanos.lexiconlair.definition.domain.Definition;
import com.ronanos.lexiconlair.definition.persistence.DefinitionRepository;
import com.ronanos.lexiconlair.security.SpringSecurityConfiguration;
import com.ronanos.lexiconlair.user.domain.User;
import com.ronanos.lexiconlair.user.persistence.UserRepository;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
    private UserRepository userRepository;

    @MockitoBean
    private BookWordRepository bookWordRepository;

    @MockitoBean
    private DefinitionRepository definitionRepository;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private User currentUser() {
        User u = new User();
        u.setId(1L);
        return u;
    }

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
        when(wordDefinitionService.saveWordWithDefinitions(any(Word.class), any()))
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

        verify(wordDefinitionService).saveWordWithDefinitions(
                argThat(word -> word.getText().equals("ephemeral") && word.getLanguage().equals("en")),
                any());
    }

    @Test
    void createWordSetsCreatedAtAndCreatedBy() throws Exception {
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser()));
        Word saved = new Word("ephemeral", "en");
        saved.setCreatedBy(1L);
        when(wordDefinitionService.saveWordWithDefinitions(any(Word.class), anyLong())).thenReturn(saved);

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
                .andExpect(jsonPath("$.createdBy").value(1));

        verify(wordDefinitionService).saveWordWithDefinitions(
                argThat(word -> word.getCreatedAt() != null && Long.valueOf(1L).equals(word.getCreatedBy())),
                anyLong());
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
    void updateWordSetsUpdatedAtAndUpdatedBy() throws Exception {
        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser()));
        Word existing = new Word("oldword", "en");
        when(wordRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(wordRepository.save(any(Word.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/api/words/1")
                        .with(user("ronan").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "newword",
                                  "language": "en"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedBy").value(1));

        verify(wordRepository).save(argThat(word ->
                word.getUpdatedAt() != null && Long.valueOf(1L).equals(word.getUpdatedBy())));
    }

    @Test
    void searchWordsReturnsResultsAcrossAllUsers() throws Exception {
        when(bookWordRepository.searchAll("eph")).thenReturn(List.of());
        when(wordRepository.searchByText("eph")).thenReturn(List.of());

        mockMvc.perform(get("/api/words/search?q=eph").with(user("ronan").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void searchWordsReturnsSavedWordsWithoutBookContext() throws Exception {
        Word stoic = new Word("stoic", "en");
        ReflectionTestUtils.setField(stoic, "id", 12L);

        when(bookWordRepository.searchAll("stoic")).thenReturn(List.of());
        when(wordRepository.searchByText("stoic")).thenReturn(List.of(stoic));
        when(definitionRepository.findByWord_Id(12L)).thenReturn(List.of());

        mockMvc.perform(get("/api/words/search?q=stoic").with(user("ronan").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookWordId").doesNotExist())
                .andExpect(jsonPath("$[0].bookId").doesNotExist())
                .andExpect(jsonPath("$[0].word.text").value("stoic"))
                .andExpect(jsonPath("$[0].word.language").value("en"));
    }

    @Test
    void listWordsWithoutDefinitionsReturnsWordsMissingDefinitions() throws Exception {
        Word rakish = new Word("rakish", "en");
        ReflectionTestUtils.setField(rakish, "id", 14L);
        when(wordRepository.findWithoutDefinitions()).thenReturn(List.of(rakish));

        mockMvc.perform(get("/api/words/without-definitions").with(user("ronan").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(14))
                .andExpect(jsonPath("$[0].text").value("rakish"));
    }

    @Test
    void refreshWordDefinitionsCallsDictionaryRefresh() throws Exception {
        Word rakish = new Word("rakish", "en");
        ReflectionTestUtils.setField(rakish, "id", 14L);
        Definition definition = new Definition();
        definition.setWord(rakish);
        definition.setDefinitionText("Having a dashing appearance.");
        definition.setPartOfSpeech("adjective");
        definition.setSourceApi("dictionaryapi.dev");

        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser()));
        when(wordRepository.findById(14L)).thenReturn(Optional.of(rakish));
        when(wordDefinitionService.refreshDefinitions(rakish, 1L)).thenReturn(List.of(definition));

        mockMvc.perform(post("/api/words/14/definitions/refresh").with(user("ronan").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.word.text").value("rakish"))
                .andExpect(jsonPath("$.definitions[0].definitionText").value("Having a dashing appearance."));
    }

    @Test
    void refreshMissingDefinitionsRefreshesAllDefinitionlessWords() throws Exception {
        Word rakish = new Word("rakish", "en");
        ReflectionTestUtils.setField(rakish, "id", 14L);
        Definition definition = new Definition();
        definition.setWord(rakish);
        definition.setDefinitionText("Having a dashing appearance.");
        definition.setPartOfSpeech("adjective");
        definition.setSourceApi("dictionaryapi.dev");

        when(userRepository.findByUsername("ronan")).thenReturn(Optional.of(currentUser()));
        when(wordRepository.findWithoutDefinitions()).thenReturn(List.of(rakish));
        when(wordDefinitionService.refreshDefinitions(rakish, 1L)).thenReturn(List.of(definition));

        mockMvc.perform(post("/api/words/definitions/refresh-missing").with(user("ronan").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].word.text").value("rakish"))
                .andExpect(jsonPath("$[0].definitions[0].definitionText").value("Having a dashing appearance."));
    }

    @Test
    void getWordReturns404WhenMissing() throws Exception {
        when(wordRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/words/99").with(user("ronan").roles("USER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Word not found"));
    }

    @Test
    void deleteWordReturns204() throws Exception {
        mockMvc.perform(delete("/api/words/1").with(user("ronan").roles("USER")))
                .andExpect(status().isNoContent());

        verify(wordRepository).deleteById(1L);
    }
}
