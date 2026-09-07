package com.workoutdone.rpgym.health.outbox.exception;

/**
 * Kafka 발행 실패.
 *
 * 클라이언트 요청 흐름이 아니라 백그라운드 발행기에서만 발생하므로 ErrorCode를 두지 않는다.
 * 발행기가 잡아서 재시도 / DLQ로 처리한다.
 */
public class EventPublishException extends RuntimeException {

    public EventPublishException(String topic, Throwable cause) {
        super("Kafka 발행에 실패했습니다. topic=" + topic, cause);
    }
}