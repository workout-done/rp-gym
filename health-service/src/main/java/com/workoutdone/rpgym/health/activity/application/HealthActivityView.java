package com.workoutdone.rpgym.health.activity.application;

import com.workoutdone.rpgym.health.activity.domain.ActivitySnapshot;
import com.workoutdone.rpgym.health.activity.domain.ActivitySource;
import com.workoutdone.rpgym.health.activity.domain.HealthActivity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record HealthActivityView(
        UUID activityId,
        UUID userId,
        LocalDate activityDate,
        Instant measuredAt,
        int steps,
        int activeMinutes,
        int activeCalories,
        ActivitySource source
) {
    public static HealthActivityView of(HealthActivity activity) {
        ActivitySnapshot snapshot = activity.getSnapshot();
        return new HealthActivityView(
                activity.getActivityId(),
                activity.getUserId(),
                activity.getActivityDate(),
                activity.getMeasuredAt(),
                snapshot.getSteps(),
                snapshot.getActiveMinutes(),
                snapshot.getActiveCalories(),
                activity.getSource()
        );
    }

    /** 아직 동기화 이력이 없는 사용자의 오늘 데이터 (404가 아니라 0값으로 응답한다) */
    public static HealthActivityView empty(UUID userId, LocalDate activityDate) {
        ActivitySnapshot zero = ActivitySnapshot.zero();
        return new HealthActivityView(
                null, userId, activityDate, null,
                zero.getSteps(), zero.getActiveMinutes(), zero.getActiveCalories(),
                null
        );
    }
}