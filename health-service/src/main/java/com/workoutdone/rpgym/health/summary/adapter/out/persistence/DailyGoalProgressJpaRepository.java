package com.workoutdone.rpgym.health.summary.adapter.out.persistence;

import com.workoutdone.rpgym.health.summary.domain.DailyGoalProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DailyGoalProgressJpaRepository extends JpaRepository<DailyGoalProgress, UUID> {

    List<DailyGoalProgress> findBySummaryId(UUID summaryId);

    List<DailyGoalProgress> findByUserIdAndActivityDateBetween(UUID userId, LocalDate from, LocalDate to);
}