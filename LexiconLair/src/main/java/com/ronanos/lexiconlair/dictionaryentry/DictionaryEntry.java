package com.ronanos.lexiconlair.dictionaryentry;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.Size;

@Entity(name = "dictionaryEntry")
public class DictionaryEntry {	

		
	public DictionaryEntry() {
		
	}
	
	public DictionaryEntry(int id, String entryWord, String definition, LocalDate dateAdded, String username, boolean externalLookupCompleted) {
		super();
		this.id = id;
		this.entryWord = entryWord;
		this.definition = definition;
		this.dateAdded = dateAdded;
		this.username = username;		
		this.externalLookupCompleted = externalLookupCompleted;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_seq")
	@SequenceGenerator(name = "my_seq", initialValue = 1001, allocationSize = 50)
	private int id;
	//	
	private String username;
	private String entryWord;
	@Size(min=5, message="Minimum length is 5 characters")
	private String definition;	
	private LocalDate dateAdded;
	private boolean externalLookupCompleted;

	public String getEntryWord() {
		return entryWord;
	}

	public void setEntryWord(String entryWord) {
		this.entryWord = entryWord;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;		
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public LocalDate getDateAdded() {
		return dateAdded;
	}

	public void setDateAdded(LocalDate dateAdded) {
		this.dateAdded = dateAdded;
	}

	public boolean isExternalLookupCompleted() {
		return externalLookupCompleted;
	}

	public void setExternalLookupCompleted(boolean externalLookupCompleted) {
		this.externalLookupCompleted = externalLookupCompleted;
	}

	@Override
	public String toString() {		
		return "Dictionary Entry [id = " + id + ", entryWord=" + entryWord + ", definition=" + definition + ", "
				+ ", username=" + username + "dateAdded=" + dateAdded + ", externalLookupCompleted=" + externalLookupCompleted + "]";			
	}

}
