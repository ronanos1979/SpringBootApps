package com.ronanos.lexiconlair.word.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity(name = "word")
public class Word {

	public Word() {
	}

	public Word(String text, String locale) {
		this.text = text;
		this.locale = locale;
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
	private String locale;

	public Long getId() {
		return id;
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
