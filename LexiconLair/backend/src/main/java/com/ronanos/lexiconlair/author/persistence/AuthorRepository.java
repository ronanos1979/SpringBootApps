package com.ronanos.lexiconlair.author.persistence;

import com.ronanos.lexiconlair.author.domain.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {
}
