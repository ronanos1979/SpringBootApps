package com.ronanos.lexiconlair.definition.persistence;

import com.ronanos.lexiconlair.definition.domain.Definition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DefinitionRepository extends JpaRepository<Definition, Long> {

    List<Definition> findByWord_Id(Long wordId);

    void deleteByWord_Id(Long wordId);

    @Query("SELECT d FROM Definition d WHERE d.word.id <> :wordId")
    List<Definition> findByWord_IdNot(@Param("wordId") Long wordId);
}
