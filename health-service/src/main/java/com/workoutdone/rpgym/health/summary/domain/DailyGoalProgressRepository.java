package com.workoutdone.rpgym.health.summary.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DailyGoalProgressRepository {

    DailyGoalProgress save(DailyGoalProgress progress);

    List<DailyGoalProgress> saveAll(List<DailyGoalProgress> progresses);

    List<DailyGoalProgress> findBySummaryId(UUID summaryId);

    List<DailyGoalProgress> findByUserIdAndActivityDateBetween(UUID userId, LocalDate from, LocalDate to);
}