package com.workoutdone.rpgym.game.quest.domain.repo;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.workoutdone.rpgym.game.quest.domain.aggregate.Quest;

public interface QuestRepository {
	Optional<Quest> findById(UUID questId);
	Quest save(Quest quest);
	Optional<Quest> findActiveByUserId(UUID userId, Instant now);
}
