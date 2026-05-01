package com.ronanos.lexiconlair.definition.domain;


import com.ronanos.lexiconlair.word.domain.Word;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name="definition")
public class Definition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String definitionText;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false)
    private String partOfSpeech;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String example;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String sourceApi;

    private LocalDateTime cachedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public String getDefinitionText() {
        return definitionText;
    }

    public void setDefinitionText(String definitionText) {
        this.definitionText = definitionText;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public String getSourceApi() {
        return sourceApi;
    }

    public void setSourceApi(String sourceApi) {
        this.sourceApi = sourceApi;
    }

    public LocalDateTime getCachedAt() {
        return cachedAt;
    }

    public void setCachedAt(LocalDateTime cachedAt) {
        this.cachedAt = cachedAt;
    }

    @Override
    public String toString() {
        return "Definition{" +
                "id=" + id +
                ", word=" + word +
                ", definitionText='" + definitionText + '\'' +
                ", partOfSpeech='" + partOfSpeech + '\'' +
                ", example='" + example + '\'' +
                ", sourceApi='" + sourceApi + '\'' +
                ", cachedAt=" + cachedAt +
                '}';
    }
}
