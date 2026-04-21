package com.ronanos.lexiconlair.word;

import com.ronanos.lexiconlair.word.domain.Word;
import com.ronanos.lexiconlair.word.persistence.WordRepository;
import com.ronanos.lexiconlair.word.web.WordController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WordControllerTest {

    @Mock
    private WordRepository wordRepository;

    @InjectMocks
    private WordController controller;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listAllWordsAddsRepositoryResultsToModel() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("ronan", "password"));
        ModelMap model = new ModelMap();
        List<Word> words = List.of(new Word("lexicon", "en"), new Word("lair", "en"));
        when(wordRepository.findAll()).thenReturn(words);

        String viewName = controller.listAllWords(model);

        assertEquals("word/listWords", viewName);
        assertSame(words, model.get("words"));
    }

    @Test
    void showNewWordPageAddsBlankWordToModel() {
        ModelMap model = new ModelMap();

        String viewName = controller.showNewWordPage(model);

        assertEquals("word/addWord", viewName);
        Word word = (Word) model.get("word");
        assertEquals("", word.getText());
        assertEquals("", word.getLocale());
    }

    @Test
    void addNewWordReturnsFormViewWhenValidationFails() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("ronan", "password"));
        ModelMap model = new ModelMap();
        Word word = new Word("hi", "en");
        BindingResult result = new BeanPropertyBindingResult(word, "word");
        result.rejectValue("text", "Size", "Minimum length is 5 characters");

        String viewName = controller.addNewWord(model, word, result);

        assertEquals("word/addWord", viewName);
        verify(wordRepository, never()).save(word);
    }

    @Test
    void addNewWordSavesWordAndRedirectsWhenValidationPasses() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("ronan", "password"));
        ModelMap model = new ModelMap();
        Word word = new Word("lexicon", "en");
        BindingResult result = new BeanPropertyBindingResult(word, "word");

        String viewName = controller.addNewWord(model, word, result);

        assertEquals("redirect:list-words", viewName);
        verify(wordRepository).save(word);
    }

    @Test
    void showUpdateWordPageLoadsWordFromRepository() {
        ModelMap model = new ModelMap();
        Word existingWord = new Word("update", "en");
        when(wordRepository.findById(anyLong())).thenReturn(Optional.of(existingWord));

        String viewName = controller.showUpdateWordPage(12L, model);

        assertEquals("word/addWord", viewName);
        assertSame(existingWord, model.get("word"));
    }

    @Test
    void updateWordReturnsFormViewWhenValidationFails() {
        ModelMap model = new ModelMap();
        Word word = new Word("hi", "en");
        BindingResult result = new BeanPropertyBindingResult(word, "word");
        result.rejectValue("text", "Size", "Minimum length is 5 characters");

        String viewName = controller.updateWord(model, word, result);

        assertEquals("word/addWord", viewName);
        verify(wordRepository, never()).save(word);
    }

    @Test
    void updateWordSavesWordAndRedirectsWhenValidationPasses() {
        ModelMap model = new ModelMap();
        Word word = new Word("updated", "en");
        BindingResult result = new BeanPropertyBindingResult(word, "word");

        String viewName = controller.updateWord(model, word, result);

        assertEquals("redirect:list-words", viewName);
        verify(wordRepository).save(word);
    }

    @Test
    void deleteWordDeletesByIdAndRedirects() {
        String viewName = controller.deleteWord(99L);

        assertEquals("redirect:list-words", viewName);
        verify(wordRepository).deleteById(99L);
    }
}
