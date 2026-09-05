package com.workoutdone.rpgym.health.summary.adapter.out.persistence;

import com.workoutdone.rpgym.health.summary.domain.DailyGoalProgress;
import com.workoutdone.rpgym.health.summary.domain.DailyGoalProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DailyGoalProgressRepositoryImpl implements DailyGoalProgressRepository {

    private final DailyGoalProgressJpaRepository jpaRepository;

    @Override
    public DailyGoalProgress save(DailyGoalProgress progress) {
        return jpaRepository.save(progress);
    }

    @Override
    public List<DailyGoalProgress> saveAll(List<DailyGoalProgress> progresses) {
        return jpaRepository.saveAll(progresses);
    }

    @Override
    public List<DailyGoalProgress> findBySummaryId(UUID summaryId) {
        return jpaRepository.findBySummaryId(summaryId);
    }

    @Override
    public List<DailyGoalProgress> findByUserIdAndActivityDateBetween(UUID userId, LocalDate from, LocalDate to) {
        return jpaRepository.findByUserIdAndActivityDateBetween(userId, from, to);
    }
}