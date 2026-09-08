package com.workoutdone.rpgym.health.summary.application;

import com.workoutdone.rpgym.health.summary.domain.DailyGoalProgress;
import com.workoutdone.rpgym.health.summary.domain.DailyGoalProgressRepository;
import com.workoutdone.rpgym.health.summary.domain.DailyHealthSummary;
import com.workoutdone.rpgym.health.summary.domain.DailyHealthSummaryRepository;
import com.workoutdone.rpgym.health.summary.domain.MetricType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HealthSummarySyncService {

    private final DailyHealthSummaryRepository summaryRepository;
    private final DailyGoalProgressRepository progressRepository;
    // TODO: User Service health-contexts 조회 클라이언트 (다음 작업)

    // HealthSummarySyncService 클래스 상단에 추가
    private static final BigDecimal DEFAULT_STEP_GOAL = BigDecimal.valueOf(5000);
    private static final BigDecimal DEFAULT_ACTIVE_MINUTES_GOAL = BigDecimal.valueOf(60);
    private static final BigDecimal DEFAULT_ACTIVE_CALORIES_GOAL = BigDecimal.valueOf(300);

    @Transactional
    public void sync(UUID userId, UUID activityId, LocalDate activityDate,
                     int steps, int activeMinutes, int activeCalories, Instant measuredAt) {

        Instant now = Instant.now();

        DailyHealthSummary summary = summaryRepository
                .findByUserIdAndActivityDate(userId, activityDate)
                .orElseGet(() -> createInitialSummaryAndProgress(userId, activityDate, now));

        boolean applied = summary.applySync(steps, activeMinutes, activeCalories, measuredAt, now);
        if (!applied) {
            return;
        }
        summaryRepository.save(summary);

        List<DailyGoalProgress> progresses = progressRepository.findBySummaryId(summary.getSummaryId());
        for (DailyGoalProgress progress : progresses) {
            BigDecimal achievedValue = resolveAchievedValue(progress.getMetricType(), steps, activeMinutes, activeCalories);
            progress.updateAchieved(achievedValue);
        }
        progressRepository.saveAll(progresses);

        boolean allAchieved = !progresses.isEmpty()
                && progresses.stream().allMatch(DailyGoalProgress::isAchieved);
        if (allAchieved) {
            boolean newlyAchieved = summary.markAllGoalsAchieved(now);
            summaryRepository.save(summary);
        }
    }

    private DailyHealthSummary createInitialSummaryAndProgress(UUID userId, LocalDate activityDate, Instant now) {
        DailyHealthSummary summary = DailyHealthSummary.createFor(userId, activityDate, now);
        summaryRepository.save(summary);

        progressRepository.saveAll(List.of(
                DailyGoalProgress.createFor(summary.getSummaryId(), userId, activityDate, MetricType.STEPS, DEFAULT_STEP_GOAL),
                DailyGoalProgress.createFor(summary.getSummaryId(), userId, activityDate, MetricType.ACTIVE_MINUTES, DEFAULT_ACTIVE_MINUTES_GOAL),
                DailyGoalProgress.createFor(summary.getSummaryId(), userId, activityDate, MetricType.ACTIVE_CALORIES, DEFAULT_ACTIVE_CALORIES_GOAL)
        ));
        return summary;
    }

    private BigDecimal resolveAchievedValue(MetricType metricType, int steps, int activeMinutes, int activeCalories) {
        return switch (metricType) {
            case STEPS -> BigDecimal.valueOf(steps);
            case ACTIVE_MINUTES -> BigDecimal.valueOf(activeMinutes);
            case ACTIVE_CALORIES -> BigDecimal.valueOf(activeCalories);
        };
    }
}