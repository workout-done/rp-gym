package com.workoutdone.rpgym.health.activity.application.event;

import com.workoutdone.rpgym.health.activity.domain.HealthActivity;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * HealthActivitySynced 이벤트의 data 필드. 기획서 13-1 스키마 그대로다.
 *
 * 델타가 아니라 누적값을 싣는다. 이벤트가 하나 유실돼도 다음 동기화가 자동으로 메운다.
 * Game Service는 이 누적값과 Quest의 baselineValue를 비교해 진행/완료를 판단한다.
 */
public record HealthActivitySyncedData(
        LocalDate activityDate,
        OffsetDateTime measuredAt,
        Cumulative cumulative
) {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public record Cumulative(int steps, int activeMinutes, int activeCalories) {
    }

    public static HealthActivitySyncedData from(HealthActivity activity) {
        return new HealthActivitySyncedData(
                activity.getActivityDate(),
                activity.getMeasuredAt().atZone(KST).toOffsetDateTime(),
                new Cumulative(
                        activity.getSnapshot().getSteps(),
                        activity.getSnapshot().getActiveMinutes(),
                        activity.getSnapshot().getActiveCalories()
                )
        );
    }
}