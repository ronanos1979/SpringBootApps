package com.ronanos.lexiconlair.word.persistence;

import com.ronanos.lexiconlair.word.domain.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WordRepository extends JpaRepository<Word, Long> {

    Optional<Word> findByTextIgnoreCaseAndLanguage(String text, String language);

    @Query("SELECT w FROM word w WHERE LOWER(w.text) LIKE LOWER(CONCAT('%', :q, '%')) ORDER BY w.text")
    List<Word> searchByText(@Param("q") String q);

    @Query("SELECT w FROM word w WHERE NOT EXISTS (SELECT d FROM Definition d WHERE d.word = w) ORDER BY w.text")
    List<Word> findWithoutDefinitions();

    @Query("SELECT DISTINCT d.word FROM Definition d")
    List<Word> findWordsWithDefinitions();
}
