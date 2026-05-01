package com.ronanos.lexiconlair.dictionary;

import com.ronanos.lexiconlair.dictionary.client.DictionaryApiClient;
import com.ronanos.lexiconlair.dictionary.dto.DictionaryWordDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DictionaryApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private DictionaryApiClient dictionaryApiClient;

    @Test
    void getEntriesReturnsApiResponseWhenDictionaryLookupSucceeds() {
        DictionaryWordDTO expected = new DictionaryWordDTO();
        expected.setWord("lexicon");

        DictionaryWordDTO[] expectedResponse = { expected };
        String expectedUrl = "https://api.dictionaryapi.dev/api/v2/entries/en/lexicon";

        when(restTemplate.getForObject(expectedUrl, DictionaryWordDTO[].class))
                .thenReturn(expectedResponse);

        List<DictionaryWordDTO> actual = dictionaryApiClient.getEntries("lexicon", "en");

        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertSame(expected, actual.getFirst());
    }

    @Test
    void getEntriesReturnsEmptyListWhenApiReturnsNull() {
        String expectedUrl = "https://api.dictionaryapi.dev/api/v2/entries/en/ghostword";

        when(restTemplate.getForObject(expectedUrl, DictionaryWordDTO[].class))
                .thenReturn(null);

        List<DictionaryWordDTO> actual = dictionaryApiClient.getEntries("ghostword", "en");

        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    @Test
    void getEntriesReturnsEmptyListWhenApiThrowsException() {
        String expectedUrl = "https://api.dictionaryapi.dev/api/v2/entries/en/errorword";

        when(restTemplate.getForObject(expectedUrl, DictionaryWordDTO[].class))
                .thenThrow(new RestClientException("dictionary api unavailable"));

        List<DictionaryWordDTO> actual = dictionaryApiClient.getEntries("errorword", "en");

        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    @Test
    void getFirstEntryReturnsFirstEntryWhenResultsExist() {
        DictionaryWordDTO expected = new DictionaryWordDTO();
        expected.setWord("lexicon");

        when(restTemplate.getForObject("https://api.dictionaryapi.dev/api/v2/entries/en/lexicon",
                DictionaryWordDTO[].class))
                .thenReturn(new DictionaryWordDTO[] { expected });

        var actual = dictionaryApiClient.getFirstEntry("lexicon", "en");

        assertTrue(actual.isPresent());
        assertSame(expected, actual.get());
    }
}
