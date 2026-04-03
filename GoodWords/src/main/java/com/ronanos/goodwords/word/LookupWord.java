package com.ronanos.goodwords.word;

import java.time.LocalDate;

import com.ronanos.goodwords.dictionary.DictionaryService;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;

@Entity(name = "Word")
public class LookupWord {
	public LookupWord() {
		
	}
	
	public LookupWord(int id, String username, String lookupWord, String description, LocalDate targetDate, boolean done) {
		super();
		this.id = id;
		this.username = username;
		this.lookupWord = lookupWord;
		this.description = description;
		this.targetDate = targetDate;
		this.done = done;
	}
	
	@Id
	@GeneratedValue
	private int id;
	
//	@Column (name="username")
	private String username;
	@Size(min=5, message="Minimum length is 5 characters")
	private String lookupWord;
	private String description;
	private LocalDate targetDate;
	private boolean done;
	
	public String getLookupWord() {
		return lookupWord;
	}

	public void setLookupWord(String lookupWord) {
		this.lookupWord = lookupWord;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDate getTargetDate() {
		return targetDate;
	}

	public void setTargetDate(LocalDate targetDate) {
		this.targetDate = targetDate;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	@Override
	public String toString() {
		return "Word [id=" + id + ", username=" + username + ", lookupWord=" + lookupWord + ", description=" + description + ", targetDate="
				+ targetDate + ", done=" + done + "]";
	}

	

}
