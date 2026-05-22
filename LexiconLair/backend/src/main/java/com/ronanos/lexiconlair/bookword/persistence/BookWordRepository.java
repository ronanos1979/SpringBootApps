package com.ronanos.lexiconlair.bookword.persistence;

import com.ronanos.lexiconlair.bookword.domain.BookWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookWordRepository extends JpaRepository<BookWord, Long> {

    List<BookWord> findByBook_IdOrderByCreatedAtDesc(Long bookId);

    List<BookWord> findByBook_IdAndCreatedByOrderByCreatedAtDesc(Long bookId, Long createdBy);

    boolean existsByBook_IdAndWord_Id(Long bookId, Long wordId);

    @Query("SELECT bw FROM BookWord bw WHERE bw.createdBy = :userId AND LOWER(bw.word.text) LIKE LOWER(CONCAT('%', :q, '%')) ORDER BY bw.word.text")
    List<BookWord> searchByUser(@Param("userId") Long userId, @Param("q") String q);

    @Query("SELECT bw FROM BookWord bw WHERE LOWER(bw.word.text) LIKE LOWER(CONCAT('%', :q, '%')) ORDER BY bw.word.text, bw.createdAt DESC")
    List<BookWord> searchAll(@Param("q") String q);
}
