package com.ronanos.lexiconlair.word.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

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

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private Long createdBy;
	private Long updatedBy;

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

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

	public Long getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(Long updatedBy) {
		this.updatedBy = updatedBy;
	}

	@Override
	public String toString() {
		return "Word{" +
				"id=" + id +
				", text='" + text + '\'' +
				", language='" + language + '\'' +
				", createdAt=" + createdAt +
				", updatedAt=" + updatedAt +
				", createdBy=" + createdBy +
				", updatedBy=" + updatedBy +
				'}';
	}
}
