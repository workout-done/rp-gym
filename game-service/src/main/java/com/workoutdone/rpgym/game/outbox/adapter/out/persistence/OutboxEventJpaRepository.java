package com.workoutdone.rpgym.game.outbox.adapter.out.persistence;

import com.workoutdone.rpgym.game.outbox.domain.OutboxStatus;
import com.workoutdone.rpgym.game.outbox.domain.aggregate.OutboxEvent;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEvent, UUID> {

    /**
     * idx_outbox_events_status (status, created_at)를 그대로 탄다.
     *
     * PESSIMISTIC_WRITE            -> SELECT ... FOR UPDATE
     * lock.timeout = -2            -> SKIP LOCKED (Hibernate LockOptions.SKIP_LOCKED)
     *     0  : 잠겨 있으면 즉시 에러
     *    -1  : 무한정 기다림
     *    -2  : 잠긴 건 건너뛰기      <- 이것
     *
     * 다른 인스턴스가 이미 집어간 행을 기다리지 않고 건너뛰므로 라운드가 서로를 막지 않는다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("select o from OutboxEvent o where o.status = :status order by o.createdAt asc")
    List<OutboxEvent> findByStatusForUpdate(@Param("status") OutboxStatus status, Pageable pageable);
}
