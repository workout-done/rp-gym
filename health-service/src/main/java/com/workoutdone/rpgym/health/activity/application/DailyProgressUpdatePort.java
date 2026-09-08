package com.workoutdone.rpgym.health.activity.application;

/**
 * 건강 활동이 저장·갱신된 뒤 일일 요약 갱신을 요청하는 통로.
 *
 * Activity 컨텍스트는 Summary가 무엇을 계산하는지 알지 않는다.
 * 구현은 Health Summary 컨텍스트가 제공한다. (EventOutboxPort와 같은 구조)
 *
 * 반드시 동기화 트랜잭션 안에서 호출된다.
 * DailyGoalCompleted를 같은 트랜잭션의 Outbox에 기록해야 Transactional Outbox가 성립하므로,
 * 구현체에 @Transactional(propagation = Propagation.MANDATORY)를 권장한다.
 *
 * 호출 시점은 두 군데다.
 *  - 신규 저장 직후 (HealthActivitySynced를 Outbox에 기록한 뒤)
 *  - 재동기화로 누적값이 갱신된 직후
 */
public interface DailyProgressUpdatePort {

    void applySync(SyncedActivity syncedActivity);
}