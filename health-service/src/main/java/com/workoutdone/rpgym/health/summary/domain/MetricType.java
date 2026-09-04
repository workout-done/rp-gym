package com.workoutdone.rpgym.health.summary.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MetricType {

    STEPS("steps"),
    ACTIVE_MINUTES("min"),
    ACTIVE_CALORIES("kcal");

    private final String unit;
}