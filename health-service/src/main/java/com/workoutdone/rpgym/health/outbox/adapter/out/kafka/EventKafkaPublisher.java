package com.workoutdone.rpgym.health.outbox.adapter.out.kafka;

import com.workoutdone.rpgym.health.outbox.application.EventPublisherPort;
import com.workoutdone.rpgym.health.outbox.config.OutboxPublishProperties;
import com.workoutdone.rpgym.health.outbox.exception.EventPublishException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Outbox payload를 Kafka로 내보내는 어댑터.
 *
 * 발행 결과를 확인해야 Outbox 상태를 PUBLISHED로 전이할 수 있으므로 비동기 전송을 기다린다.
 * (fire-and-forget으로 두면 브로커가 받지 못한 이벤트를 PUBLISHED로 찍게 된다)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventKafkaPublisher implements EventPublisherPort {

    private static final String HEADER_ORIGINAL_TOPIC = "x-original-topic";
    private static final String HEADER_RETRY_COUNT = "x-retry-count";
    private static final String HEADER_ERROR_MESSAGE = "x-error-message";

    /** 헤더에 스택트레이스(어떤 메서드들을 거쳐서 여기까지 왔는지 호출 경로를 기록한 목록) 전문이 들어가지 않도록 자른다 */
    private static final int ERROR_MESSAGE_MAX_LENGTH = 500;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxPublishProperties properties;

    @Override
    public void publish(String topic, String partitionKey, String payload) {
        send(new ProducerRecord<>(topic, partitionKey, payload));
    }

    @Override
    public void publishToDlq(String dlqTopic,
                             String partitionKey,
                             String payload,
                             String originalTopic,
                             int retryCount,
                             String errorMessage) {

        ProducerRecord<String, String> record =
                new ProducerRecord<>(dlqTopic, partitionKey, payload);

        record.headers().add(HEADER_ORIGINAL_TOPIC, bytes(originalTopic));
        record.headers().add(HEADER_RETRY_COUNT, bytes(String.valueOf(retryCount)));
        record.headers().add(HEADER_ERROR_MESSAGE, bytes(abbreviate(errorMessage)));

        send(record);
    }

    private void send(ProducerRecord<String, String> record) {
        try {
            kafkaTemplate.send(record)
                    .get(properties.sendTimeout().toMillis(), TimeUnit.MILLISECONDS);

            log.debug("Kafka 발행 완료. topic={} key={}", record.topic(), record.key());

        } catch (InterruptedException e) {
            // 인터럽트 상태를 삼키면 상위에서 종료 신호를 놓친다
            Thread.currentThread().interrupt();
            throw new EventPublishException(record.topic(), e);

        } catch (ExecutionException | TimeoutException e) {
            throw new EventPublishException(record.topic(), e);
        }
    }

    private byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private String abbreviate(String message) {
        if (message == null) {
            return "";
        }
        return message.length() <= ERROR_MESSAGE_MAX_LENGTH
                ? message
                : message.substring(0, ERROR_MESSAGE_MAX_LENGTH);
    }
}