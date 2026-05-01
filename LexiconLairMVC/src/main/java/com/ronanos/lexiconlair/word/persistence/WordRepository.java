package com.ronanos.lexiconlair.word.persistence;

import com.ronanos.lexiconlair.word.domain.Word;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordRepository extends JpaRepository<Word, Long>{
	
//	public List<LookupWord> findByUsername(String username);
// 	public List<LookupWord> findById(Long id);

}
