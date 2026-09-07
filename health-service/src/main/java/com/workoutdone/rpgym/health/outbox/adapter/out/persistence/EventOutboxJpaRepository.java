package com.workoutdone.rpgym.health.outbox.adapter.out.persistence;

import com.workoutdone.rpgym.health.outbox.domain.EventOutbox;
import com.workoutdone.rpgym.health.outbox.domain.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventOutboxJpaRepository extends JpaRepository<EventOutbox, UUID> {

    boolean existsByDedupKey(String dedupKey);

    /** idx_health_activity_outbox_status (status, created_at) 사용 */
    List<EventOutbox> findByStatusOrderByCreatedAtAsc(OutboxStatus status, Pageable pageable);
}