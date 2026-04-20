package com.ronanos.lexiconlair.word;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import jakarta.validation.Valid;

@Controller
@SessionAttributes("name")
public class WordController {

	private WordRepository wordRepository;

    public WordController(WordRepository wordRepository) {
		super();		
		this.wordRepository = wordRepository;
	}


	@RequestMapping("list-words")
	public String listAllWords(ModelMap model) {
		String username = getLoggedInUsername(model);
		// TODO need to change this so that we filter by words that belong to this user.
		List<Word> words = wordRepository.findAll();
		model.addAttribute("words", words);
		return "listWords";
	}

	@RequestMapping(value="add-word", method=RequestMethod.GET)
	public String showNewWordPage(ModelMap model) {
		Word word = new Word(0, "", "");
		model.put("word", word);
		return "addWord";
	}

	@RequestMapping(value="add-word", method=RequestMethod.POST)
	public String addNewWord(ModelMap model, @Valid Word word, BindingResult result) {
		System.out.println("Incoming Word: " + word);
		if (result.hasErrors()) {
			return "addWord";
		}			
		String username = getLoggedInUsername(model);
		wordRepository.save(word);
		return "redirect:list-words";
	}

	@RequestMapping(value="update-word", method=RequestMethod.GET)
	public String showUpdateWordPage(@RequestParam int id, ModelMap model) {
		Word word = wordRepository.findById(id).get();
		model.addAttribute("word", word);
		return "addWord";
	}
	
	@RequestMapping(value="update-word", method=RequestMethod.POST)
	// Before Command Bean / Form Backing Object
	// public String addNewword(@RequestParam String description, ModelMap model) {
	public String updateWord(ModelMap model, @Valid Word word, BindingResult result) {
		if (result.hasErrors()) {
			return "addWord";
		}			
		
//		String username = getLoggedInUsername(model);
		wordRepository.save(word);
		return "redirect:list-words";
	}

	@RequestMapping("delete-word")
	public String deleteWord(@RequestParam int id) {
		wordRepository.deleteById(id);
		return "redirect:list-words";
	}	

	private String getLoggedInUsername(ModelMap model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();		
	}
}
