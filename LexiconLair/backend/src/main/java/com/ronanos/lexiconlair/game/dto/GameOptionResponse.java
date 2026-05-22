package com.ronanos.lexiconlair.game.dto;

public record GameOptionResponse(
        Long definitionId,
        String definitionText,
        String partOfSpeech
) {
}
