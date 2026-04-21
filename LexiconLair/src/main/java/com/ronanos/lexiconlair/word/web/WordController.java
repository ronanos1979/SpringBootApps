package com.ronanos.lexiconlair.word.web;

import java.util.List;

import com.ronanos.lexiconlair.word.domain.Word;
import com.ronanos.lexiconlair.word.persistence.WordRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.server.ResponseStatusException;

@Controller
@SessionAttributes("name")
public class WordController {

	private static final Logger logger = LoggerFactory.getLogger(WordController.class);

	private final WordRepository wordRepository;

    public WordController(WordRepository wordRepository) {
		this.wordRepository = wordRepository;
	}


	@RequestMapping("list-words")
	public String listAllWords(ModelMap model) {
		List<Word> words = wordRepository.findAll();
		model.addAttribute("words", words);
		return "word/listWords";
	}

	@RequestMapping(value = "add-word", method = RequestMethod.GET)
	public String showNewWordPage(ModelMap model) {
		model.put("word", new Word("", ""));
		return "word/addWord";
	}

	@RequestMapping(value = "add-word", method = RequestMethod.POST)
	public String addNewWord(ModelMap model, @Valid @ModelAttribute("word") Word word, BindingResult result) {
		if (result.hasErrors()) {
			return "word/addWord";
		}
		wordRepository.save(word);
		logger.info("Created word {}", word.getText());
		return "redirect:list-words";
	}

	@RequestMapping(value = "update-word", method = RequestMethod.GET)
	public String showUpdateWordPage(@RequestParam Long id, ModelMap model) {
		Word word = wordRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Word not found"));
		model.addAttribute("word", word);
		return "word/addWord";
	}
	
	@RequestMapping(value = "update-word", method = RequestMethod.POST)
	public String updateWord(ModelMap model, @Valid @ModelAttribute("word") Word word, BindingResult result) {
		if (result.hasErrors()) {
			return "word/addWord";
		}
		wordRepository.save(word);
		logger.info("Updated word {}", word.getText());
		return "redirect:list-words";
	}

	@PostMapping("delete-word")
	public String deleteWord(@RequestParam Long id) {
		wordRepository.deleteById(id);
		return "redirect:list-words";
	}
}
