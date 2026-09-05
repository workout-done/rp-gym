package com.workoutdone.rpgym.health.activity.adapter.in.web.dto;

import com.workoutdone.rpgym.health.activity.application.HealthActivityView;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** 건강 활동 조회/동기화 응답. 이력이 없는 경우 activityId·measuredAt·source가 null이다. */
public record HealthActivityResponse(
        UUID activityId,
        UUID userId,
        LocalDate activityDate,
        Instant measuredAt,
        int steps,
        int activeMinutes,
        int activeCalories,
        String source
) {
    public static HealthActivityResponse from(HealthActivityView view) {
        return new HealthActivityResponse(
                view.activityId(),
                view.userId(),
                view.activityDate(),
                view.measuredAt(),
                view.steps(),
                view.activeMinutes(),
                view.activeCalories(),
                view.source() == null ? null : view.source().name()
        );
    }
}