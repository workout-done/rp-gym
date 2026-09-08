package com.workoutdone.rpgym.game.outbox.domain.repo;

import com.workoutdone.rpgym.game.outbox.domain.aggregate.OutboxEvent;

public interface OutboxEventRepository {
	OutboxEvent save(OutboxEvent event);
}
