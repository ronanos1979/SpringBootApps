package com.ronanos.lexiconlair.dictionary;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ronanos.lexiconlair.dictionary.DictionaryWordDTO;

import org.springframework.http.ResponseEntity;

@Service
public class DictionaryService {

    private final String apiUrl = "https://api.dictionaryapi.dev/api/v2/entries/en/{word}";

    private final RestTemplate restTemplate;

    public DictionaryService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public DictionaryWordDTO getMeaning(String word) {
        String url = apiUrl.replace("{word}", word);

        try {
            // Make the API call
            // ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);

        	DictionaryWordDTO response;
        	
            // Make the REST API call and map the JSON response to a WordMeaning object
            response = restTemplate.getForObject(url, DictionaryWordDTO.class, word);
            
            
            if (response == null) {
            	return new DictionaryWordDTO();			
            } else {
            	return response;
            }
//            // Simplified for brevity, you can add logic to extract meaning from response
//            if (response.getStatusCode().is2xxSuccessful()) {
//                // Assuming the response has a structure that gives us the definition.
//                return "Meaning for " + word + " " + response;
//            } else {
//                return "Meaning not found.";
//            }
        } catch (Exception e) {
        	e.printStackTrace();
            return new DictionaryWordDTO();
            // "Error fetching meaning: " + e.getMessage();
        }
    }
}
