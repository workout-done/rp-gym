package com.workoutdone.rpgym.health.summary.adapter.in;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record QuestSuggestedPayload(
        UUID suggestionId,
        LocalDate activityDate,
        Instant basedOnMeasuredAt,
        String title,
        String metric,
        int targetValue
) {
}