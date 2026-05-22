package com.ronanos.lexiconlair.dictionary.client;

import com.ronanos.lexiconlair.dictionary.dto.DictionaryWordDTO;

import java.util.List;

public record DictionaryLookupResult(
        List<DictionaryWordDTO> entries,
        String status,
        Integer httpStatus,
        String message
) {
    public static DictionaryLookupResult success(List<DictionaryWordDTO> entries) {
        return new DictionaryLookupResult(entries, "SUCCESS", 200, null);
    }

    public static DictionaryLookupResult noResults(String message) {
        return new DictionaryLookupResult(List.of(), "NO_RESULTS", 200, message);
    }

    public static DictionaryLookupResult failure(Integer httpStatus, String message) {
        return new DictionaryLookupResult(List.of(), "FAILED", httpStatus, message);
    }
}
