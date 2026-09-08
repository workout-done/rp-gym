package com.workoutdone.rpgym.game.outbox.adapter.out.persistence;

import com.workoutdone.rpgym.game.outbox.domain.aggregate.OutboxEvent;
import com.workoutdone.rpgym.game.outbox.domain.repo.OutboxEventRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OutboxEventRepositoryImpl implements OutboxEventRepository {

    private final OutboxEventJpaRepository outboxEventJpaRepository;

    @Override
    public OutboxEvent save(OutboxEvent event) {
        return outboxEventJpaRepository.save(event);
    }
}
