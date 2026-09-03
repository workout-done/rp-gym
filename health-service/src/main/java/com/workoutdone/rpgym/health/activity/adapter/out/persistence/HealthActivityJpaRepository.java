package com.workoutdone.rpgym.health.activity.adapter.out.persistence;

import com.workoutdone.rpgym.health.activity.domain.HealthActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface HealthActivityJpaRepository extends JpaRepository<HealthActivity, UUID> {

    Optional<HealthActivity> findByUserIdAndMeasuredAt(UUID userId, Instant measuredAt);

    /** idx_health_activities_user_date (user_id, activity_date, measured_at DESC) 사용 */
    Optional<HealthActivity> findFirstByUserIdAndActivityDateOrderByMeasuredAtDesc(
            UUID userId, LocalDate activityDate);
}
