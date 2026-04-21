package com.ronanos.lexiconlair.dictionary;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DictionaryServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private DictionaryService dictionaryService;

    @Test
    void getMeaningReturnsApiResponseWhenDictionaryLookupSucceeds() {
        DictionaryWordDTO expected = new DictionaryWordDTO();
        expected.setWord("lexicon");
        String expectedUrl = "https://api.dictionaryapi.dev/api/v2/entries/en/lexicon";
        when(restTemplate.getForObject(expectedUrl, DictionaryWordDTO.class)).thenReturn(expected);

        DictionaryWordDTO actual = dictionaryService.getMeaning("lexicon");

        assertSame(expected, actual);
    }

    @Test
    void getMeaningReturnsEmptyDtoWhenApiReturnsNull() {
        String expectedUrl = "https://api.dictionaryapi.dev/api/v2/entries/en/ghostword";
        when(restTemplate.getForObject(expectedUrl, DictionaryWordDTO.class)).thenReturn(null);

        DictionaryWordDTO actual = dictionaryService.getMeaning("ghostword");

        assertNotNull(actual);
    }

    @Test
    void getMeaningReturnsEmptyDtoWhenApiThrowsException() {
        String expectedUrl = "https://api.dictionaryapi.dev/api/v2/entries/en/errorword";
        when(restTemplate.getForObject(expectedUrl, DictionaryWordDTO.class))
                .thenThrow(new RestClientException("dictionary api unavailable"));

        DictionaryWordDTO actual = dictionaryService.getMeaning("errorword");

        assertNotNull(actual);
    }
}
