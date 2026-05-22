package com.ronanos.lexiconlair.book.persistence;

import com.ronanos.lexiconlair.book.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByCreatedByOrderByTitleAsc(Long createdBy);

    Optional<Book> findByTitleIgnoreCase(String title);

    List<Book> findTop10ByTitleContainingIgnoreCaseOrderByTitleAsc(String title);
}
