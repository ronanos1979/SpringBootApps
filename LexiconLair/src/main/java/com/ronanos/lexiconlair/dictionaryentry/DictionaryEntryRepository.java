package com.ronanos.lexiconlair.dictionaryentry;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DictionaryEntryRepository extends JpaRepository<DictionaryEntry, Integer>{
	
//	public List<LookupWord> findByUsername(String username);
// 	public List<LookupWord> findById(Integer id); 

}
