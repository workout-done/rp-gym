package com.workoutdone.rpgym.game.outbox.domain.repo;

import com.workoutdone.rpgym.game.outbox.domain.aggregate.OutboxEvent;

import java.util.List;

public interface OutboxEventRepository {

	OutboxEvent save(OutboxEvent event);

	/**
	 * 미발행(PENDING) 이벤트를 오래된 순으로 조회하며 행 잠금을 건다.
	 *
	 * 발행기가 다중 인스턴스로 늘어나도 같은 행을 두 인스턴스가 집어 중복 발행하는 것을 막는다.
	 * 이미 잠긴 행은 기다리지 않고 건너뛴다.
	 *
	 * limit을 int로 받는다. Pageable을 받으면 Spring Data 타입이 도메인으로 새어 들어온다.
	 */
	List<OutboxEvent> findPendingForUpdate(int limit);
}
