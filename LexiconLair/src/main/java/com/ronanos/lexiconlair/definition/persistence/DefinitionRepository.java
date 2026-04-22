package com.ronanos.lexiconlair.definition.persistence;

import com.ronanos.lexiconlair.definition.domain.Definition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DefinitionRepository extends JpaRepository<Definition, Long> {
}
