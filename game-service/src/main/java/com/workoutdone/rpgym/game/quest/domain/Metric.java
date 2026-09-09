package com.workoutdone.rpgym.game.quest.domain;

import java.util.Arrays;
import java.util.Optional;

public enum Metric {
    STEPS,
    ACTIVE_MINUTES,
    ACTIVE_CALORIES;

    public static Optional<Metric> from(String value) {
        return Arrays.stream(values())
                .filter(metric -> metric.name().equals(value))
                .findFirst();
    }
}
