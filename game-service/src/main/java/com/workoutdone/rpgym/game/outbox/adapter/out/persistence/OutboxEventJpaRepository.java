package com.workoutdone.rpgym.game.outbox.adapter.out.persistence;

import com.workoutdone.rpgym.game.outbox.domain.aggregate.OutboxEvent;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEvent, UUID> {
}
