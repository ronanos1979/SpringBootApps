package com.ronanos.lexiconlair.game.service;

import com.ronanos.lexiconlair.admin.service.AdminSettingsService;
import com.ronanos.lexiconlair.bookword.persistence.BookWordRepository;
import com.ronanos.lexiconlair.definition.domain.Definition;
import com.ronanos.lexiconlair.definition.persistence.DefinitionRepository;
import com.ronanos.lexiconlair.game.dto.GameOptionResponse;
import com.ronanos.lexiconlair.game.dto.GameQuestionResponse;
import com.ronanos.lexiconlair.word.domain.Word;
import com.ronanos.lexiconlair.word.persistence.WordRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@Service
public class GameService {

    private final BookWordRepository bookWordRepository;
    private final DefinitionRepository definitionRepository;
    private final WordRepository wordRepository;
    private final AdminSettingsService adminSettingsService;
    private final Random random = new Random();

    public GameService(
            BookWordRepository bookWordRepository,
            DefinitionRepository definitionRepository,
            WordRepository wordRepository,
            AdminSettingsService adminSettingsService) {
        this.bookWordRepository = bookWordRepository;
        this.definitionRepository = definitionRepository;
        this.wordRepository = wordRepository;
        this.adminSettingsService = adminSettingsService;
    }

    public GameQuestionResponse nextQuestion(String mode, Long userId) {
        String normalizedMode = mode == null ? "easy" : mode.toLowerCase(Locale.ROOT);
        List<Word> candidates = switch (normalizedMode) {
            case "easy" -> bookWordRepository.findDistinctWordsByCreatedBy(userId);
            case "difficult" -> wordRepository.findWordsWithDefinitions();
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported game mode");
        };

        List<Word> playableWords = candidates.stream()
                .filter(word -> !definitionRepository.findByWord_Id(word.getId()).isEmpty())
                .toList();

        if (playableWords.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No playable words available");
        }

        Word word = playableWords.get(random.nextInt(playableWords.size()));
        List<Definition> correctDefinitions = definitionRepository.findByWord_Id(word.getId());
        Definition correct = correctDefinitions.get(random.nextInt(correctDefinitions.size()));

        int requestedOptions = Math.max(2, adminSettingsService.getSettings().getGameOptionCount());
        List<Definition> decoys = new ArrayList<>(definitionRepository.findByWord_IdNot(word.getId()));
        Collections.shuffle(decoys);

        List<Definition> options = new ArrayList<>();
        options.add(correct);
        decoys.stream()
                .limit(Math.max(0, requestedOptions - 1L))
                .forEach(options::add);
        Collections.shuffle(options);

        return new GameQuestionResponse(
                normalizedMode,
                word.getId(),
                word.getText(),
                word.getLanguage(),
                correct.getId(),
                options.stream()
                        .map(def -> new GameOptionResponse(def.getId(), def.getDefinitionText(), def.getPartOfSpeech()))
                        .toList());
    }
}
