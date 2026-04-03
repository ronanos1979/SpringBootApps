package com.ronanos.goodwords.word;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LookupWordRepository extends JpaRepository<LookupWord, Integer>{
	
	public List<LookupWord> findByUsername(String username);

}
