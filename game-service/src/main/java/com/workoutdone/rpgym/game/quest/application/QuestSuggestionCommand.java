package com.workoutdone.rpgym.game.quest.application;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record QuestSuggestionCommand(
        UUID userId,
        UUID suggestionId,
        LocalDate activityDate,
        Instant basedOnMeasuredAt,
        String title,
        String metric,
        int targetValue
) {
}
