package com.ronanos.goodwords.word;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;

@Service
public class LookupWordService {
	@Autowired
	private static List<LookupWord> words = new ArrayList<>();
	
	private static int wordsCount = 0;
	
	static {
		words.add(new LookupWord(++wordsCount, "John", "Dog", "An Animal", 
				LocalDate.now().plusYears(1), false));
		words.add(new LookupWord(++wordsCount, "Ronan", "Howto", "A description of how to", 
				LocalDate.now().plusYears(2), false));
		words.add(new LookupWord(++wordsCount, "Ronan", "Thesaurus", "A list of alternative words", 
				LocalDate.now().plusYears(3), false));
	}
	
	public List<LookupWord> findByUsername(String username) {
		Predicate<? super LookupWord> predicate 
			= word -> word.getUsername().equalsIgnoreCase(username);					
		return words.stream().filter(predicate).toList();
	}
	
	public void addWord(String name, String word, String description, LocalDate targetDate, boolean isDone) {		
		LookupWord theWord = new LookupWord(++wordsCount, name, word, description, targetDate, isDone);		
		words.add(theWord);		
	}
	
	public void deleteById(int id) {
		Predicate<? super LookupWord> predicate 
			= word -> word.getId() == id;
		words.removeIf(predicate);
	}	

	public LookupWord findById(int id) {
		Predicate<? super LookupWord> predicate 
		= word -> word.getId() == id;
		LookupWord word = words.stream().filter(predicate).findFirst().get();
		return word;
	}

	public void updateWord(@Valid LookupWord word) {
		deleteById(word.getId());
		words.add(word);
	}

}
