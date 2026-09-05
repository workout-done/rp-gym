package com.workoutdone.rpgym.health.outbox.application;

import com.workoutdone.rpgym.health.outbox.domain.HealthEventType;

import java.util.UUID;

/**
 * Health Service의 각 컨텍스트가 이벤트를 Outbox에 기록하는 통로.
 * Health Summary 도메인도 이 포트를 통해 DailyGoalCompleted를 기록한다.
 * 반드시 도메인 저장과 같은 트랜잭션 안에서 호출한다.
 */
public interface EventOutboxPort {

    /**
     * @param eventId          payload 본문의 eventId이자 event_id 컬럼 값. 재전송에도 불변이며,
     *                         Game Service가 중복 처리 방지 키로 사용한다. (팀 합의)
     *                         QuestSuggested처럼 payload 안에 이 값을 다시 실어야 하는 이벤트가 있어
     *                         호출자가 생성해서 넘긴다.
     * @param eventType        발행할 이벤트 유형
     * @param userId           Kafka 파티션 키 (같은 사용자 이벤트의 순서 보장)
     * @param sourceActivityId 이 이벤트를 유발한 health_activities 행 (NOT NULL)
     * @param dedupKey         이벤트 중복 발행 방지 키 (150자 이내)
     * @param data             payload의 data 필드에 담길 객체
     * @return 새로 기록했으면 true, dedupKey 중복이라 건너뛰었으면 false
     */
    boolean append(UUID eventId,
                   HealthEventType eventType,
                   UUID userId,
                   UUID sourceActivityId,
                   String dedupKey,
                   Object data);
}