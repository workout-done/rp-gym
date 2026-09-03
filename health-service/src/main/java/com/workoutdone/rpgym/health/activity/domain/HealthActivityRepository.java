package com.workoutdone.rpgym.health.activity.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface HealthActivityRepository {

    HealthActivity save(HealthActivity healthActivity);

    /** 중복 동기화 멱등 처리용 조회 */
    Optional<HealthActivity> findByUserIdAndMeasuredAt(UUID userId, Instant measuredAt);

    /** 해당 일자의 가장 최신 누적 스냅샷 (오늘의 건강 활동 조회) */
    Optional<HealthActivity> findLatestSnapshot(UUID userId, LocalDate activityDate);
}
