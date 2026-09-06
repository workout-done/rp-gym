package com.workoutdone.rpgym.health.outbox.config;

import com.workoutdone.rpgym.health.outbox.domain.HealthEventType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

/**
 * Outbox 발행기 설정.
 *
 * 토픽명은 Game Service 컨슈머와 맞춰야 하므로 코드가 아니라 설정으로 둔다.
 */
@ConfigurationProperties(prefix = "rpgym.outbox")
public record OutboxPublishProperties(

        /** 한 라운드에 조회할 PENDING 최대 건수 */
        @DefaultValue("100") int pollSize,

        /** 이 횟수만큼 실패하면 DLQ로 보내고 FAILED로 종료한다 */
        @DefaultValue("5") int maxRetry,

        /** Kafka 발행 응답 대기 시간 */
        @DefaultValue("5s") Duration sendTimeout,

        /** DLQ 토픽 접미사 */
        @DefaultValue(".dlq") String dlqSuffix,

        /** 이벤트 타입별 토픽 매핑 */
        Map<HealthEventType, String> topics
) {

    public OutboxPublishProperties {
        topics = (topics == null) ? new EnumMap<>(HealthEventType.class) : topics;
    }

    /**
     * 매핑이 없으면 조용히 넘기지 않고 즉시 실패시킨다.
     * 토픽 누락은 이벤트가 영영 전달되지 않는 문제라 로그로만 남기면 늦게 발견된다.
     */
    public String topicOf(HealthEventType eventType) {
        String topic = topics.get(eventType);
        if (topic == null || topic.isBlank()) {
            throw new IllegalStateException(
                    "토픽 매핑이 없습니다. rpgym.outbox.topics 설정을 확인하세요. eventType=" + eventType);
        }
        return topic;
    }

    public String dlqTopicOf(String topic) {
        return topic + dlqSuffix;
    }
}