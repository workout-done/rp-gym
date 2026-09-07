package com.workoutdone.rpgym.health.outbox.adapter.out.persistence;

import com.workoutdone.rpgym.health.outbox.domain.EventOutbox;
import com.workoutdone.rpgym.health.outbox.domain.EventOutboxRepository;
import com.workoutdone.rpgym.health.outbox.domain.OutboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EventOutboxRepositoryImpl implements EventOutboxRepository {

    private final EventOutboxJpaRepository eventOutboxJpaRepository;

    @Override
    public EventOutbox save(EventOutbox eventOutbox) {
        return eventOutboxJpaRepository.save(eventOutbox);
    }

    @Override
    public boolean existsByDedupKey(String dedupKey) {
        return eventOutboxJpaRepository.existsByDedupKey(dedupKey);
    }

    @Override
    public List<EventOutbox> findPending(int limit) {
        return eventOutboxJpaRepository.findByStatusOrderByCreatedAtAsc(
                OutboxStatus.PENDING, PageRequest.of(0, limit));
    }
}