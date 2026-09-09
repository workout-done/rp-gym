package com.workoutdone.rpgym.health.summary.application;

import com.workoutdone.rpgym.health.summary.domain.MetricType;

public interface QuestSuggestionAiPort {
    String generateTitle(MetricType metricType, int shortageValue);
}