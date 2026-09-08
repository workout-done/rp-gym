package com.workoutdone.rpgym.health.outbox.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Outbox 발행기 설정.
 *
 * 토픽은 Game Service 컨슈머와 맞춰야 하므로 코드가 아니라 설정으로 둔다.
 * 이벤트 종류별로 나누지 않는 이유는 순서 보장이다. 같은 sync에서
 * HealthActivitySynced와 QuestSuggested가 함께 나가는데, 토픽이 분리되면
 * 각각 소비되어 Quest baseline이 옛날 누적값으로 고정될 수 있다. (팀 합의)
 */
@ConfigurationProperties(prefix = "rpgym.outbox")
public record OutboxPublishProperties(

        // 한 라운드에 조회할 PENDING 최대 건수
        @DefaultValue("100") int pollSize,

        // 이 횟수만큼 실패하면 DLQ로 보내고 FAILED로 종료한다
        @DefaultValue("5") int maxRetry,

        // Kafka 발행 응답 대기 시간
        @DefaultValue("5s") Duration sendTimeout,

        // Health Service가 발행하는 모든 이벤트의 토픽
        @DefaultValue("health.events") String topic,

        // DLQ 토픽 접미사
        @DefaultValue(".dlq") String dlqSuffix
) {
    public String dlqTopic() {
        return topic + dlqSuffix;
    }
}