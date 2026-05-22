package com.ronanos.lexiconlair.bookword.dto;

import java.util.List;

public record BulkAddWordsResponse(
        List<BookWordResponse> added,
        List<String> duplicates,
        List<String> errors
) {}
