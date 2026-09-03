package com.workoutdone.rpgym.game.quest.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface QuestRepository {
	Optional<Quest> findById(UUID questId);
	Quest save(Quest quest);
	Optional<Quest> findActiveByUserId(UUID userId, Instant now);
}
