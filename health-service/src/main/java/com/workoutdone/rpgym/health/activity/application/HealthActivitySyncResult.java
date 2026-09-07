package com.workoutdone.rpgym.health.activity.application;

/**
 * @param created 이번 요청으로 새 스냅샷이 저장됐으면 true (201/200 판단에 쓴다)
 */
public record HealthActivitySyncResult(
        HealthActivityView view, boolean created
) {
}