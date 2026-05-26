package com.ronanos.lexiconlair.game;

import com.ronanos.lexiconlair.admin.domain.AdminSettings;
import com.ronanos.lexiconlair.admin.service.AdminSettingsService;
import com.ronanos.lexiconlair.bookword.persistence.BookWordRepository;
import com.ronanos.lexiconlair.definition.domain.Definition;
import com.ronanos.lexiconlair.definition.persistence.DefinitionRepository;
import com.ronanos.lexiconlair.game.service.GameService;
import com.ronanos.lexiconlair.word.domain.Word;
import com.ronanos.lexiconlair.word.persistence.WordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private BookWordRepository bookWordRepository;

    @Mock
    private DefinitionRepository definitionRepository;

    @Mock
    private WordRepository wordRepository;

    @Mock
    private AdminSettingsService adminSettingsService;

    @Test
    void easyQuestionUsesCurrentUserWordsAndDecoysFromOtherWords() {
        Word target = word(1L, "stoic");
        Definition correct = definition(10L, target, "Enduring pain without complaint.");
        Definition decoy = definition(20L, word(2L, "rakish"), "Having a dashing appearance.");

        when(bookWordRepository.findDistinctWordsByCreatedBy(7L)).thenReturn(List.of(target));
        when(definitionRepository.findByWord_Id(1L)).thenReturn(List.of(correct));
        when(definitionRepository.findByWord_IdNot(1L)).thenReturn(List.of(decoy));
        when(adminSettingsService.getSettings()).thenReturn(new AdminSettings(1L, 50, 10, 2));

        var question = gameService().nextQuestion("easy", 7L);

        assertEquals("stoic", question.wordText());
        assertEquals(10L, question.correctDefinitionId());
        assertEquals(2, question.options().size());
        assertNotEquals(question.options().get(0).definitionId(), question.options().get(1).definitionId());
    }

    @Test
    void difficultQuestionUsesAllWordsWithDefinitions() {
        Word target = word(1L, "stoic");
        Definition correct = definition(10L, target, "Enduring pain without complaint.");

        when(wordRepository.findWordsWithDefinitions()).thenReturn(List.of(target));
        when(definitionRepository.findByWord_Id(1L)).thenReturn(List.of(correct));
        when(definitionRepository.findByWord_IdNot(1L)).thenReturn(List.of());
        when(adminSettingsService.getSettings()).thenReturn(new AdminSettings(1L, 50, 10, 4));

        var question = gameService().nextQuestion("difficult", 7L);

        assertEquals("difficult", question.mode());
        assertEquals("stoic", question.wordText());
    }

    @Test
    void nullModeDefaultsToEasyMode() {
        Word target = word(1L, "stoic");
        Definition correct = definition(10L, target, "Enduring pain without complaint.");

        when(bookWordRepository.findDistinctWordsByCreatedBy(7L)).thenReturn(List.of(target));
        when(definitionRepository.findByWord_Id(1L)).thenReturn(List.of(correct));
        when(definitionRepository.findByWord_IdNot(1L)).thenReturn(List.of());
        when(adminSettingsService.getSettings()).thenReturn(new AdminSettings(1L, 50, 10, 2));

        var question = gameService().nextQuestion(null, 7L);

        assertEquals("easy", question.mode());
    }

    @Test
    void modeIsCaseInsensitive() {
        Word target = word(1L, "stoic");
        Definition correct = definition(10L, target, "Enduring pain without complaint.");

        when(bookWordRepository.findDistinctWordsByCreatedBy(7L)).thenReturn(List.of(target));
        when(definitionRepository.findByWord_Id(1L)).thenReturn(List.of(correct));
        when(definitionRepository.findByWord_IdNot(1L)).thenReturn(List.of());
        when(adminSettingsService.getSettings()).thenReturn(new AdminSettings(1L, 50, 10, 2));

        var question = gameService().nextQuestion("EASY", 7L);

        assertEquals("easy", question.mode());
    }

    @Test
    void throwsNotFoundWhenNoPlayableWordsExist() {
        when(bookWordRepository.findDistinctWordsByCreatedBy(7L)).thenReturn(List.of());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> gameService().nextQuestion("easy", 7L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void throwsNotFoundWhenCandidateWordsHaveNoDefinitions() {
        Word wordWithoutDefs = word(1L, "stoic");

        when(bookWordRepository.findDistinctWordsByCreatedBy(7L)).thenReturn(List.of(wordWithoutDefs));
        when(definitionRepository.findByWord_Id(1L)).thenReturn(List.of());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> gameService().nextQuestion("easy", 7L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void throwsBadRequestForUnsupportedMode() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> gameService().nextQuestion("expert", 7L));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void questionOptionCountRespectsMinimuOfTwo() {
        Word target = word(1L, "stoic");
        Definition correct = definition(10L, target, "Enduring pain without complaint.");

        when(bookWordRepository.findDistinctWordsByCreatedBy(7L)).thenReturn(List.of(target));
        when(definitionRepository.findByWord_Id(1L)).thenReturn(List.of(correct));
        when(definitionRepository.findByWord_IdNot(1L)).thenReturn(List.of());
        when(adminSettingsService.getSettings()).thenReturn(new AdminSettings(1L, 50, 10, 1));

        var question = gameService().nextQuestion("easy", 7L);

        assertEquals(1, question.options().size());
    }

    @Test
    void correctDefinitionIdIsAlwaysIncludedInOptions() {
        Word target = word(1L, "stoic");
        Definition correct = definition(10L, target, "Enduring pain without complaint.");
        Definition decoy1 = definition(20L, word(2L, "rakish"), "Having a dashing appearance.");
        Definition decoy2 = definition(30L, word(3L, "ephemeral"), "Lasting a very short time.");

        when(bookWordRepository.findDistinctWordsByCreatedBy(7L)).thenReturn(List.of(target));
        when(definitionRepository.findByWord_Id(1L)).thenReturn(List.of(correct));
        when(definitionRepository.findByWord_IdNot(1L)).thenReturn(List.of(decoy1, decoy2));
        when(adminSettingsService.getSettings()).thenReturn(new AdminSettings(1L, 50, 10, 3));

        var question = gameService().nextQuestion("easy", 7L);

        boolean correctPresent = question.options().stream()
                .anyMatch(opt -> opt.definitionId().equals(question.correctDefinitionId()));
        assertEquals(true, correctPresent);
        assertEquals(10L, question.correctDefinitionId());
    }

    private GameService gameService() {
        return new GameService(bookWordRepository, definitionRepository, wordRepository, adminSettingsService);
    }

    private static Word word(Long id, String text) {
        Word word = new Word(text, "en");
        ReflectionTestUtils.setField(word, "id", id);
        return word;
    }

    private static Definition definition(Long id, Word word, String text) {
        Definition definition = new Definition();
        ReflectionTestUtils.setField(definition, "id", id);
        definition.setWord(word);
        definition.setDefinitionText(text);
        definition.setPartOfSpeech("adjective");
        definition.setSourceApi("test");
        return definition;
    }
}
