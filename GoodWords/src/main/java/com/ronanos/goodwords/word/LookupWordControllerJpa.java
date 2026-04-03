package com.ronanos.goodwords.word;

import java.time.LocalDate;
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

import com.ronanos.goodwords.dictionary.DictionaryService;

import jakarta.validation.Valid;

@Controller
@SessionAttributes("name")
public class LookupWordControllerJpa {


    private final DictionaryService dictionaryService;
    
	public LookupWordControllerJpa(LookupWordRepository wordRepository, DictionaryService dictionaryService) {
		super();		
		this.wordRepository = wordRepository;
		this.dictionaryService = dictionaryService;
	}

	private LookupWordRepository wordRepository;

	@RequestMapping("list-words")
	public String listAllWords(ModelMap model) {
		String username = getLoggedInUsername(model);			
		List<LookupWord> words = wordRepository.findByUsername(username);
		

//		for (int i = 0; i < words.size(); i++) {
//			LookupWord word = words.get(i);
//			
//			String description = dictionaryService.getMeaning(word.getLookupWord()).getMeanings().toString();
//			if (description == null) {
//				word.setDescription("No description");
//			} else {
//				word.setDescription(dictionaryService.getMeaning(word.getLookupWord()).getMeanings().toString());
//			}
//			
//		}
//		
		model.addAttribute("words", words);
		return "listWords";
	}

	@RequestMapping(value="add-word", method=RequestMethod.GET)
	public String showNewwordPage(ModelMap model) {
		String username = getLoggedInUsername(model);
		LookupWord word = new LookupWord(0, username, "", "", LocalDate.now().plusYears(1), false);
		model.put("word", word);
		return "word";
	}

	@RequestMapping(value="add-word", method=RequestMethod.POST)
	// Before Command Bean / Form Backing Object
	// public String addNewword(@RequestParam String description, ModelMap model) {
	public String addNewword(ModelMap model, @Valid LookupWord word, BindingResult result) {
		if (result.hasErrors()) {
			return "word";
		}			
		String username = getLoggedInUsername(model);
		word.setUsername(username);
		wordRepository.save(word);		
		// wordService.addword(username, word.getDescription(), word.getTargetDate(), word.isDone());
		return "redirect:list-words";
	}

	@RequestMapping(value="update-word", method=RequestMethod.GET)
	public String showUpdatewordPage(@RequestParam int id, ModelMap model) {
		//word word = wordService.findById(id);
		LookupWord word = wordRepository.findById(id).get();
		model.addAttribute("word", word);
		return "word";
	}
	
	@RequestMapping(value="update-word", method=RequestMethod.POST)
	// Before Command Bean / Form Backing Object
	// public String addNewword(@RequestParam String description, ModelMap model) {
	public String updateword(ModelMap model, @Valid LookupWord word, BindingResult result) {
		if (result.hasErrors()) {
			return "word";
		}			
		
		String username = getLoggedInUsername(model);
		word.setUsername(username);
		//wordService.updateword(word);
		wordRepository.save(word);
		return "redirect:list-words";
	}

	@RequestMapping("delete-word")
	public String deleteword(@RequestParam int id) {
		// wordService.deleteById(id);
		wordRepository.deleteById(id);
		return "redirect:list-words";
	}	

	private String getLoggedInUsername(ModelMap model) {
		//		String username = (String)model.get("name");
		//		return username;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();		
	}
}
