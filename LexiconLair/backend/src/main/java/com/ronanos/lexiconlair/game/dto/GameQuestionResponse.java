package com.ronanos.lexiconlair.game.dto;

import java.util.List;

public record GameQuestionResponse(
        String mode,
        Long wordId,
        String wordText,
        String language,
        Long correctDefinitionId,
        List<GameOptionResponse> options
) {
}
