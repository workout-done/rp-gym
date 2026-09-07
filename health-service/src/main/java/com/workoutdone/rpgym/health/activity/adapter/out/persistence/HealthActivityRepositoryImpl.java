package com.workoutdone.rpgym.health.activity.adapter.out.persistence;

import com.workoutdone.rpgym.health.activity.domain.HealthActivity;
import com.workoutdone.rpgym.health.activity.domain.HealthActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class HealthActivityRepositoryImpl implements HealthActivityRepository {

    private final HealthActivityJpaRepository healthActivityJpaRepository;

    @Override
    public HealthActivity save(HealthActivity healthActivity) {
        return healthActivityJpaRepository.save(healthActivity);
    }

    @Override
    public Optional<HealthActivity> findByUserIdAndMeasuredAt(UUID userId, Instant measuredAt) {
        return healthActivityJpaRepository.findByUserIdAndMeasuredAt(userId, measuredAt);
    }

    @Override
    public Optional<HealthActivity> findLatestSnapshot(UUID userId, LocalDate activityDate) {
        return healthActivityJpaRepository
                .findFirstByUserIdAndActivityDateOrderByMeasuredAtDesc(userId, activityDate);
    }
}
