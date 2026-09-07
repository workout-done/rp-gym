package com.workoutdone.rpgym.health.outbox.application;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.workoutdone.rpgym.health.outbox.domain.HealthEventType;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Outbox payload 공통 record. 이벤트 스키마와 1:1 대응한다.
 *
 * occurredAt은 발행 시각이라 재전송하면 바뀐다. 순서·중복 판정에 쓰지 않으며,
 * 판정 기준은 data 안의 measuredAt이다.
 */
@JsonPropertyOrder({"eventId", "eventType", "occurredAt", "userId", "data"})
public record EventEnvelope(
        UUID eventId,
        HealthEventType eventType,
        OffsetDateTime occurredAt,
        UUID userId,
        Object data
) {
}