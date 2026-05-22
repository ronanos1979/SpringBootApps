package com.ronanos.lexiconlair.definition.persistence;

import com.ronanos.lexiconlair.definition.domain.Definition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DefinitionRepository extends JpaRepository<Definition, Long> {

    List<Definition> findByWord_Id(Long wordId);

    void deleteByWord_Id(Long wordId);
}
