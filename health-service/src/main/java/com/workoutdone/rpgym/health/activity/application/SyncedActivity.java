package com.workoutdone.rpgym.health.activity.application;

import com.workoutdone.rpgym.health.activity.domain.HealthActivity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 일일 요약 갱신에 필요한 최소 정보.
 *
 * 이벤트 payload(HealthActivitySyncedData)와 분리한다.
 * 이벤트 스키마는 Game Service와의 계약이라 서비스 내부 호출과 수명이 다르다.
 */
public record SyncedActivity(

        // DailyGoalCompleted 발행 시 event_outbox.source_activity_id로 사용한다 (NOT NULL)
        UUID activityId,

        UUID userId,

        // measuredAt에서 KST 기준으로 파생된 값이다. Summary가 다시 계산하지 않는다.
        LocalDate activityDate,

        // 순서 역전 방어 기준. lastSyncedAt보다 오래되면 반영하지 않는다.
        Instant measuredAt,

        int steps,
        int activeMinutes,
        int activeCalories
) {
    public static SyncedActivity from(HealthActivity activity) {
        return new SyncedActivity(
                activity.getActivityId(),
                activity.getUserId(),
                activity.getActivityDate(),
                activity.getMeasuredAt(),
                activity.getSnapshot().getSteps(),
                activity.getSnapshot().getActiveMinutes(),
                activity.getSnapshot().getActiveCalories()
        );
    }
}