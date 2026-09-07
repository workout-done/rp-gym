package com.workoutdone.rpgym.health.outbox.application;

/**
 * 발행기가 메시지 브로커로 나가는 통로. 구현은 어댑터(EventKafkaPublisher)가 담당한다.
 *
 * payload는 Outbox에 저장된 문자열을 그대로 넘긴다.
 * 이 계층에서 역직렬화하거나 재구성하지 않는다 — eventId가 바뀌면
 * Game Service의 중복 처리 방지 키가 무력화된다.
 */
public interface EventPublisherPort {

    /**
     * @param topic        발행 대상 토픽
     * @param partitionKey Kafka 메시지 key (= userId). 같은 사용자 이벤트의 순서를 보장한다.
     * @param payload      Outbox payload 원문
     */
    void publish(String topic, String partitionKey, String payload);

    /**
     * 최대 재시도를 넘긴 이벤트를 DLQ로 보낸다. 원문 payload는 그대로 두고 진단 정보만 헤더로 싣는다.
     */
    void publishToDlq(String dlqTopic,
                      String partitionKey,
                      String payload,
                      String originalTopic,
                      int retryCount,
                      String errorMessage);
}