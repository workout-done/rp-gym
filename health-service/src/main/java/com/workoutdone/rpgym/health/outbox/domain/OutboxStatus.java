package com.workoutdone.rpgym.health.outbox.domain;

/**
 * Outbox 이벤트 발행 상태.
 * 전이는 PENDING → PUBLISHED 또는 PENDING → FAILED 만 허용한다.
 */
public enum OutboxStatus {
    PENDING, PUBLISHED, FAILED
}