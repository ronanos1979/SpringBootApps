package com.ronanos.lexiconlair.word;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WordRepository extends JpaRepository<Word, Integer>{
	
//	public List<LookupWord> findByUsername(String username);
// 	public List<LookupWord> findById(Integer id); 

}
