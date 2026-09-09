package com.workoutdone.rpgym.game.quest.domain.repo;

import java.util.Optional;
import java.util.UUID;

import com.workoutdone.rpgym.game.quest.domain.aggregate.UserLatestSnapshot;

public interface UserLatestSnapshotRepository {
	Optional<UserLatestSnapshot> findByUserId(UUID userId);
	UserLatestSnapshot save(UserLatestSnapshot snapshot);
}
