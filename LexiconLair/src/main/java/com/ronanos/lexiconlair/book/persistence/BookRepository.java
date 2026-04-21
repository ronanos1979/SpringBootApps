package com.ronanos.lexiconlair.book.persistence;

import com.ronanos.lexiconlair.book.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
