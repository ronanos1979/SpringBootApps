package com.ronanos.lexiconlair.word.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity(name = "word")
public class Word {

	public Word() {
	}

	public Word(String text, String language) {
		this.text = text;
		this.language = language;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Column(nullable = false)
	@Size(min = 3, max = 255, message = "Minimum length is 3 characters")
	private String text;

	@NotBlank
	@Size(max = 20)
	private String language;

	public Long getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	@Override
	public String toString() {
		return "Word{" +
				"id=" + id +
				", text='" + text + '\'' +
				", language='" + language + '\'' +
				'}';
	}
}
