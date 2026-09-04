package com.workoutdone.rpgym.health.outbox.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum HealthEventType {
    HEALTH_ACTIVITY_SYNCED, QUEST_SUGGESTED, DAILY_GOAL_COMPLETED
}