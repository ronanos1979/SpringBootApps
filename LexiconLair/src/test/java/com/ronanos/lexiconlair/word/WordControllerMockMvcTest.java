package com.ronanos.lexiconlair.word;

import com.ronanos.lexiconlair.security.SpringSecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(WordController.class)
@Import(SpringSecurityConfiguration.class)
class WordControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WordRepository wordRepository;

    @Test
    void listWordsReturnsRepositoryResultsForAuthenticatedUser() throws Exception {
        List<Word> words = List.of(new Word(1, "lexicon", "en"));
        when(wordRepository.findAll()).thenReturn(words);

        mockMvc.perform(get("/list-words").with(user("ronan").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("listWords"))
                .andExpect(model().attribute("words", words));
    }

    @Test
    void addWordGetReturnsFormView() throws Exception {
        mockMvc.perform(get("/add-word").with(user("ronan").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("addWord"))
                .andExpect(model().attributeExists("word"));
    }

    @Test
    void addWordPostRedirectsWhenPayloadIsValid() throws Exception {
        mockMvc.perform(post("/add-word")
                        .with(user("ronan").roles("USER"))
                        .param("id", "0")
                        .param("text", "lexicon")
                        .param("locale", "en"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("list-words"));

        verify(wordRepository).save(org.mockito.ArgumentMatchers.any(Word.class));
    }

    @Test
    void addWordPostReturnsFormWhenPayloadIsInvalid() throws Exception {
        mockMvc.perform(post("/add-word")
                        .with(user("ronan").roles("USER"))
                        .param("id", "0")
                        .param("text", "hi")
                        .param("locale", "en"))
                .andExpect(status().isOk())
                .andExpect(view().name("addWord"))
                .andExpect(model().attributeHasFieldErrors("word", "text"));

        verifyNoInteractions(wordRepository);
    }

    @Test
    void updateWordGetLoadsExistingWord() throws Exception {
        Word existingWord = new Word(11, "update", "en");
        when(wordRepository.findById(11)).thenReturn(Optional.of(existingWord));

        mockMvc.perform(get("/update-word").with(user("ronan").roles("USER")).param("id", "11"))
                .andExpect(status().isOk())
                .andExpect(view().name("addWord"))
                .andExpect(model().attribute("word", existingWord));
    }

    @Test
    void deleteWordRedirectsAfterDeletingEntity() throws Exception {
        mockMvc.perform(get("/delete-word").with(user("ronan").roles("USER")).param("id", "17"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("list-words"));

        verify(wordRepository).deleteById(17);
    }
}
