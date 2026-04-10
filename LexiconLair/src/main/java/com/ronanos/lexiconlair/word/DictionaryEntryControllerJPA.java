package com.ronanos.lexiconlair.word;

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

import com.ronanos.lexiconlair.dictionary.DictionaryService;

import jakarta.validation.Valid;

@Controller
@SessionAttributes("name")
public class DictionaryEntryControllerJPA {


//    private final DictionaryService dictionaryService;
	private DictionaryEntryRepository dictEntryRepository;
    
	//public LookupWordControllerJpa(LookupWordRepository wordRepository, DictionaryService dictionaryService) {
    public DictionaryEntryControllerJPA(DictionaryEntryRepository dictEntryRepository) {
		super();		
		this.dictEntryRepository = dictEntryRepository;
	}


	@RequestMapping("list-dictionary-entries")	
	public String listAllDictionaryEntries(ModelMap model) {
		String username = getLoggedInUsername(model);			
		List<DictionaryEntry> dictionaryEntries = dictEntryRepository.findAll();
//		List<LookupWord> words = wordRepository.findByUsername(username);
		

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
		model.addAttribute("dictionaryEntries", dictionaryEntries);
		return "listDictionaryEntries";
	}

	@RequestMapping(value="add-dictionary-entry", method=RequestMethod.GET)
	public String showNewWordPage(ModelMap model) {
		DictionaryEntry entry = new DictionaryEntry(0, "", "", LocalDate.now(), "", false);
		model.put("dictionaryEntry", entry);
		return "addDictionaryEntry";
	}

	@RequestMapping(value="add-dictionary-entry", method=RequestMethod.POST)	
	public String addNewWord(ModelMap model, @Valid DictionaryEntry word, BindingResult result) {
		System.out.println("Incoming word: " + word);
		if (result.hasErrors()) {
			return "addDictionaryEntry";
		}			
		String username = getLoggedInUsername(model);
		word.setUsername(username);
		word.setDateAdded(LocalDate.now());
		dictEntryRepository.save(word);
		return "redirect:list-dictionary-entries";
	}

	@RequestMapping(value="update-dictionary-entry", method=RequestMethod.GET)
	public String showUpdateWordPage(@RequestParam int id, ModelMap model) {
		DictionaryEntry word = dictEntryRepository.findById(id).get();
		model.addAttribute("word", word);
		return "word";
	}
	
	@RequestMapping(value="update-dictionary-entry", method=RequestMethod.POST)
	// Before Command Bean / Form Backing Object
	// public String addNewword(@RequestParam String description, ModelMap model) {
	public String updateword(ModelMap model, @Valid DictionaryEntry word, BindingResult result) {
		if (result.hasErrors()) {
			return "word";
		}			
		
		String username = getLoggedInUsername(model);
//		word.setUsername(username);
		//wordService.updateword(word);
		dictEntryRepository.save(word);
		return "redirect:list-dictionary-entries";
	}

	@RequestMapping("delete-dictionary-entry")
	public String deleteword(@RequestParam int id) {
		// wordService.deleteById(id);
		dictEntryRepository.deleteById(id);
		return "redirect:list-dictionary-entries";
	}	

	private String getLoggedInUsername(ModelMap model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();		
	}
}
