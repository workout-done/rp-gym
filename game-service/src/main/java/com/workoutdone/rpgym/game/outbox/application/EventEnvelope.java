package com.workoutdone.rpgym.game.outbox.application;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        UUID userId,
        Object data
) {
}
