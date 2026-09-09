package com.workoutdone.rpgym.health.activity.application;

public interface HealthActivitySyncUseCase {

    HealthActivitySyncResult sync(SyncHealthActivityCommand command);
}