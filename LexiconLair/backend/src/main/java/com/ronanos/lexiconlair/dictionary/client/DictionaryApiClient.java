package com.ronanos.lexiconlair.dictionary.client;

import com.ronanos.lexiconlair.dictionary.dto.DictionaryWordDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class DictionaryApiClient {

    private static final Logger logger = LoggerFactory.getLogger(DictionaryApiClient.class);
    private static final String API_URL = "https://api.dictionaryapi.dev/api/v2/entries/{language}/{word}";

    private final RestTemplate restTemplate;

    public DictionaryApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<DictionaryWordDTO> getEntries(String word, String language) {
        return lookupEntries(word, language).entries();
    }

    public DictionaryLookupResult lookupEntries(String word, String language) {
        String url = UriComponentsBuilder.fromUriString(API_URL)
                .buildAndExpand(language, word)
                .toUriString();

        try {
            DictionaryWordDTO[] response = restTemplate.getForObject(url, DictionaryWordDTO[].class);
            if (response == null) {
                return DictionaryLookupResult.noResults("Dictionary API returned no response.");
            }
            List<DictionaryWordDTO> entries = Arrays.asList(response);
            return entries.isEmpty()
                    ? DictionaryLookupResult.noResults("Dictionary API returned no entries.")
                    : DictionaryLookupResult.success(entries);
        } catch (RestClientResponseException e) {
            logger.warn("Dictionary lookup failed for word '{}'", word, e);
            return DictionaryLookupResult.failure(e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (RestClientException e) {
            logger.warn("Dictionary lookup failed for word '{}'", word, e);
            return DictionaryLookupResult.failure(null, e.getMessage());
        }
    }

    public Optional<DictionaryWordDTO> getFirstEntry(String word, String language) {
        return getEntries(word, language).stream().findFirst();
    }
}
