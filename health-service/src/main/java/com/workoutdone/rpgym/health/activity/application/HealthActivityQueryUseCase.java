package com.workoutdone.rpgym.health.activity.application;

import java.util.UUID;

public interface HealthActivityQueryUseCase {

    HealthActivityView getToday(UUID userId);
}