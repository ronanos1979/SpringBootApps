package com.ronanos.lexiconlair.word.persistence;

import com.ronanos.lexiconlair.word.domain.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WordRepository extends JpaRepository<Word, Long> {

    Optional<Word> findByTextIgnoreCaseAndLanguage(String text, String language);
}
