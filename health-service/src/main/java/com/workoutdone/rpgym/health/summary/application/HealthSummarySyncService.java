package com.workoutdone.rpgym.health.summary.application;

import com.workoutdone.rpgym.health.activity.application.SyncedActivity;
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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HealthSummarySyncService {

    private final DailyHealthSummaryRepository summaryRepository;
    private final DailyGoalProgressRepository progressRepository;
    // TODO: User Service health-contexts 조회 클라이언트 (다음 작업)

    private static final BigDecimal DEFAULT_STEP_GOAL = BigDecimal.valueOf(5000);
    private static final BigDecimal DEFAULT_ACTIVE_MINUTES_GOAL = BigDecimal.valueOf(60);
    private static final BigDecimal DEFAULT_ACTIVE_CALORIES_GOAL = BigDecimal.valueOf(300);

    @Transactional
    public void sync(SyncedActivity syncedActivity) {
        UUID userId = syncedActivity.userId();
        LocalDate activityDate = syncedActivity.activityDate();
        Instant measuredAt = syncedActivity.measuredAt();
        int steps = syncedActivity.steps();
        int activeMinutes = syncedActivity.activeMinutes();
        int activeCalories = syncedActivity.activeCalories();

        Instant now = Instant.now();

        DailyHealthSummary summary = summaryRepository.findByUserIdAndActivityDate(userId, activityDate).orElse(null);

        List<DailyGoalProgress> progresses;
        boolean isNew = summary == null;
        if (isNew) {
            summary = DailyHealthSummary.createFor(userId, activityDate, now);
        }

        boolean applied = summary.applySync(steps, activeMinutes, activeCalories, measuredAt, now);
        if (!applied) {
            return;
        }

        summary = summaryRepository.save(summary);

        if (isNew) {
            progresses = createInitialProgresses(summary, userId, activityDate);
        } else {
            progresses = progressRepository.findBySummaryId(summary.getSummaryId());
        }

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

    private List<DailyGoalProgress> createInitialProgresses(DailyHealthSummary summary, UUID userId, LocalDate activityDate) {
        List<DailyGoalProgress> progresses = List.of(
                DailyGoalProgress.createFor(summary.getSummaryId(), userId, activityDate, MetricType.STEPS, DEFAULT_STEP_GOAL),
                DailyGoalProgress.createFor(summary.getSummaryId(), userId, activityDate, MetricType.ACTIVE_MINUTES, DEFAULT_ACTIVE_MINUTES_GOAL),
                DailyGoalProgress.createFor(summary.getSummaryId(), userId, activityDate, MetricType.ACTIVE_CALORIES, DEFAULT_ACTIVE_CALORIES_GOAL)
        );
        return progressRepository.saveAll(progresses);
    }

    private BigDecimal resolveAchievedValue(MetricType metricType, int steps, int activeMinutes, int activeCalories) {
        return switch (metricType) {
            case STEPS -> BigDecimal.valueOf(steps);
            case ACTIVE_MINUTES -> BigDecimal.valueOf(activeMinutes);
            case ACTIVE_CALORIES -> BigDecimal.valueOf(activeCalories);
        };
    }
}