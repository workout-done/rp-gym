package com.workoutdone.rpgym.game.quest.adapter.in.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * HEALTH_ACTIVITY_SYNCED의 data. 증분이 아니라 누적값이다.
 *
 * measuredAt이 OffsetDateTime인 것은 Health가 KST 오프셋을 실어 보내기 때문이다.
 * 도메인 경계를 넘을 때 Instant로 바꾼다 -- Snapshot은 시간대를 모른다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record HealthActivitySyncedData(
        LocalDate activityDate,
        OffsetDateTime measuredAt,
        Cumulative cumulative
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Cumulative(int steps, int activeMinutes, int activeCalories) {
    }
}
