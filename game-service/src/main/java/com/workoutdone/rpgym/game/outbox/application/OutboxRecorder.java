package com.workoutdone.rpgym.game.outbox.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workoutdone.rpgym.game.outbox.domain.AggregateType;
import com.workoutdone.rpgym.game.outbox.domain.OutboxEventType;
import com.workoutdone.rpgym.game.outbox.domain.aggregate.OutboxEvent;
import com.workoutdone.rpgym.game.outbox.domain.repo.OutboxEventRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxRecorder {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxEvent append(
            AggregateType aggregateType,
            UUID aggregateId,
            OutboxEventType eventType,
            UUID userId,
            Instant occurredAt,
            Object data
    ) {
        UUID eventId = UUID.randomUUID();
        EventEnvelope envelope = new EventEnvelope(eventId, eventType.name(), occurredAt, userId, data);

        return outboxEventRepository.save(OutboxEvent.pending(
                UUID.randomUUID(),
                aggregateType,
                aggregateId,
                eventType,
                eventId,
                userId,
                toJson(envelope)
        ));
    }

    private String toJson(EventEnvelope envelope) {
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize " + envelope.eventType(), e);
        }
    }
}
