package com.workoutdone.rpgym.health.outbox.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * Health Service가 발행하는 이벤트 유형.
 *
 * DB 컬럼(event_outbox.event_type)과 Kafka 메시지 본문의 eventType 모두
 * eventName(PascalCase)을 사용한다. enum 이름을 그대로 쓰면 HEALTH_ACTIVITY_SYNCED가 되어
 * 명세 및 Game Service의 역직렬화와 어긋난다.
 */
@RequiredArgsConstructor
public enum HealthEventType {

    HEALTH_ACTIVITY_SYNCED("HealthActivitySynced"),
    QUEST_SUGGESTED("QuestSuggested"),
    DAILY_GOAL_COMPLETED("DailyGoalCompleted");

    private final String eventName;

    @JsonValue
    public String getEventName() {
        return eventName;
    }

    public static HealthEventType from(String eventName) {
        return Arrays.stream(values())
                .filter(type -> type.eventName.equals(eventName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "알 수 없는 이벤트 유형입니다: " + eventName));
    }
}