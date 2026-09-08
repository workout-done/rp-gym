package com.workoutdone.rpgym.game.outbox.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Outbox 발행기 설정.
 *
 * 토픽을 코드가 아니라 설정으로 두는 이유는 소비 측(Notification)과 맞춰야 하는 값이기 때문이다.
 * QUEST_CREATED와 QUEST_COMPLETED를 한 토픽으로 보내고 소비 측이 eventType 헤더로 분기한다.
 * Health가 health.events 하나로 통합한 것과 같은 형태다.
 *
 * ※ topic 기본값은 팀 합의 전 잠정값이다.
 */
@ConfigurationProperties(prefix = "rpgym.outbox")
public record OutboxPublishProperties(

        // 한 라운드에 집을 PENDING 최대 건수.
        // 이 트랜잭션이 열려 있는 동안 해당 행들이 잠기므로 무작정 키우지 않는다.
        @DefaultValue("100") int pollSize,

        // Kafka 발행 응답 대기 시간.
        // 프로듀서의 delivery.timeout.ms 보다 짧으면 여기서 포기한 뒤 프로듀서가 나중에 성공할 수 있다.
        // 그때 같은 이벤트가 두 번 나가지만 컨슈머가 event_id로 멱등하므로 안전하다(at-least-once).
        @DefaultValue("5s") Duration sendTimeout,

        // Game Service가 발행하는 모든 이벤트의 토픽
        @DefaultValue("game.events") String topic,

        // 재시도가 이 횟수를 넘으면 로그를 WARN에서 ERROR로 올린다.
        // 상태는 여전히 PENDING이다 -- 사람이 봐야 한다는 신호일 뿐 포기하는 것이 아니다.
        @DefaultValue("10") int escalateAfter
) {
}
