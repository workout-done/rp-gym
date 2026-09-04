package com.workoutdone.rpgym.health.outbox.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workoutdone.rpgym.health.outbox.domain.EventOutbox;
import com.workoutdone.rpgym.health.outbox.domain.EventOutboxRepository;
import com.workoutdone.rpgym.health.outbox.domain.HealthEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventOutboxAppender implements EventOutboxPort {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final EventOutboxRepository eventOutboxRepository;
    private final ObjectMapper objectMapper;

    /**
     * MANDATORY — 호출자의 트랜잭션이 없으면 즉시 예외.
     * Outbox 기록이 도메인 저장 트랜잭션에서 떨어져 나가는 순간 Transactional Outbox가 무의미해지므로,
     * 잘못된 호출을 조용히 통과시키지 않는다.
     */
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean append(UUID eventId,
                          HealthEventType eventType,
                          UUID userId,
                          UUID sourceActivityId,
                          String dedupKey,
                          Object data) {

        if (eventOutboxRepository.existsByDedupKey(dedupKey)) {
            log.debug("이미 기록된 이벤트라 생략한다. dedupKey={}", dedupKey);
            return false;
        }

        EventEnvelope envelope = new EventEnvelope(
                eventId,
                eventType,
                OffsetDateTime.now(KST),
                userId,
                data
        );

        eventOutboxRepository.save(EventOutbox.pending(
                UUID.randomUUID(),   // outbox_id — 내부 식별자. event_id와 용도가 다르므로 별도로 둔다.
                eventId,
                eventType,
                sourceActivityId,
                dedupKey,
                userId.toString(),
                serialize(envelope)
        ));

        log.debug("Outbox 기록 완료. eventType={} eventId={}", eventType, eventId);
        return true;
    }

    private String serialize(EventEnvelope envelope) {
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException e) {
            // 클라이언트가 만들 수 없는 상황 → 서버 오류 (API 공통 규격 4절)
            throw new IllegalStateException(
                    "이벤트 payload 직렬화에 실패했습니다. eventId=" + envelope.eventId(), e);
        }
    }
}