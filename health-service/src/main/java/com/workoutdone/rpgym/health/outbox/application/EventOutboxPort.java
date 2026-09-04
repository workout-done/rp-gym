package com.workoutdone.rpgym.health.outbox.application;

import com.workoutdone.rpgym.health.outbox.domain.HealthEventType;

import java.util.UUID;

/**
 * Health Service의 각 컨텍스트가 이벤트를 Outbox에 기록하는 통로.
 *
 * Health Summary 도메인도 이 포트를 통해 DailyGoalCompleted를 기록한다.
 * 반드시 도메인 저장과 같은 트랜잭션 안에서 호출한다.
 */
public interface EventOutboxPort {

    /**
     * @param eventType        발행할 이벤트 유형
     * @param userId           Kafka 파티션 키로 사용된다 (같은 사용자 이벤트의 순서 보장)
     * @param sourceActivityId 이 이벤트를 유발한 health_activities 행 (NOT NULL)
     * @param dedupKey         이벤트 중복 발행 방지 키
     * @param data             payload의 data 필드에 담길 객체
     * @return 새로 기록했으면 true, dedupKey 중복이라 건너뛰었으면 false
     */
    boolean append(HealthEventType eventType,
                   UUID userId,
                   UUID sourceActivityId,
                   String dedupKey,
                   Object data);
}