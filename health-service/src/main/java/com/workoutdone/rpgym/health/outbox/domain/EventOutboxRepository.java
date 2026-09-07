package com.workoutdone.rpgym.health.outbox.domain;

import java.util.List;

/**
 * 포트 — 순수 자바 인터페이스.
 * Health Summary 도메인도 이 컨텍스트를 통해 이벤트를 기록한다.
 */
public interface EventOutboxRepository {

    EventOutbox save(EventOutbox eventOutbox);

    /** 이벤트 중복 발행 방지 */
    boolean existsByDedupKey(String dedupKey);

    /** 미발행(PENDING) 이벤트를 오래된 순으로 조회 */
    List<EventOutbox> findPending(int limit);
}