package com.ronanos.lexiconlair.word;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.Size;

@Entity(name = "word")
public class Word {
		
	public Word() {
	}
	
	public Word(int id, String text, String locale) {
		super();
		this.id = id;
		this.text = text;
		this.locale = locale;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_seq")
	@SequenceGenerator(name = "my_seq", initialValue = 1001, allocationSize = 50)
	private int id;
	//
	@Size(min=3, message="Minimum length is 5 characters")
	private String text;
	private String locale;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;		
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	@Override
	public String toString() {
		return "Word{" +
				"id=" + id +
				", text='" + text + '\'' +
				", locale='" + locale + '\'' +
				'}';
	}
}
