package com.ronanos.lexiconlair.dictionary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class DictionaryService {

    private static final Logger logger = LoggerFactory.getLogger(DictionaryService.class);
    private static final String API_URL = "https://api.dictionaryapi.dev/api/v2/entries/en/{word}";

    private final RestTemplate restTemplate;

    public DictionaryService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public DictionaryWordDTO getMeaning(String word) {
        String url = UriComponentsBuilder.fromUriString(API_URL)
                .buildAndExpand(word)
                .toUriString();

        try {
            DictionaryWordDTO response = restTemplate.getForObject(url, DictionaryWordDTO.class);
            return response != null ? response : new DictionaryWordDTO();
        } catch (RestClientException e) {
            logger.warn("Dictionary lookup failed for word '{}'", word, e);
            return new DictionaryWordDTO();
        }
    }
}
