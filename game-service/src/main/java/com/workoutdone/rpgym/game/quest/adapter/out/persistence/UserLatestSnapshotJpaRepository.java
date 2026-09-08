package com.workoutdone.rpgym.game.quest.adapter.out.persistence;

import com.workoutdone.rpgym.game.quest.domain.aggregate.UserLatestSnapshot;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserLatestSnapshotJpaRepository extends JpaRepository<UserLatestSnapshot, UUID> {
}
