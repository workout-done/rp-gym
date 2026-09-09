package com.workoutdone.rpgym.health.summary.application;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record DeficientGoalDetectedEvent(
        UUID userId,
        UUID summaryId,
        UUID activityId,
        LocalDate activityDate,
        Instant measuredAt,
        String mostDeficientMetric,
        int shortageValue
) {
}