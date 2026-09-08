package com.workoutdone.rpgym.health.summary.application;

import com.workoutdone.rpgym.health.activity.application.SyncedActivity;
import com.workoutdone.rpgym.health.summary.domain.DailyGoalProgress;
import com.workoutdone.rpgym.health.summary.domain.DailyGoalProgressRepository;
import com.workoutdone.rpgym.health.summary.domain.DailyHealthSummary;
import com.workoutdone.rpgym.health.summary.domain.DailyHealthSummaryRepository;
import com.workoutdone.rpgym.health.summary.domain.MetricType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HealthSummarySyncService {

    private final DailyHealthSummaryRepository summaryRepository;
    private final DailyGoalProgressRepository progressRepository;
    private final ApplicationEventPublisher eventPublisher;
    // TODO: User Service health-contexts 조회 클라이언트 (다음 작업)

    private static final BigDecimal DEFAULT_STEP_GOAL = BigDecimal.valueOf(5000);
    private static final BigDecimal DEFAULT_ACTIVE_MINUTES_GOAL = BigDecimal.valueOf(60);
    private static final BigDecimal DEFAULT_ACTIVE_CALORIES_GOAL = BigDecimal.valueOf(300);

    // shortage 동률일 때 우선순위 (STEPS > ACTIVE_MINUTES > ACTIVE_CALORIES)
    private static final List<MetricType> METRIC_PRIORITY = List.of(
            MetricType.STEPS, MetricType.ACTIVE_MINUTES, MetricType.ACTIVE_CALORIES
    );

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
            // TODO(#63 이전 논의): newlyAchieved == true 일 때 DAILY_GOAL_COMPLETED를 Outbox에 적재한다.
            //            Game Service 업적·보상 연동과 함께 트러블슈팅 기간에 구현하기로 팀 합의.
            summary.markAllGoalsAchieved(now);
            summaryRepository.save(summary);
        } else {
            publishDeficientGoalEventIfNeeded(summary, syncedActivity.activityId(), progresses);
        }
    }

    private void publishDeficientGoalEventIfNeeded(DailyHealthSummary summary, UUID activityId, List<DailyGoalProgress> progresses) {
        Optional<DailyGoalProgress> mostDeficient = progresses.stream()
                .filter(p -> !p.isAchieved())
                .max(Comparator
                        .comparing(DailyGoalProgress::getShortageValue)
                        .thenComparing(p -> METRIC_PRIORITY.indexOf(p.getMetricType()), Comparator.reverseOrder())
                );

        mostDeficient.ifPresent(progress -> eventPublisher.publishEvent(new DeficientGoalDetectedEvent(
                summary.getUserId(),
                summary.getSummaryId(),
                activityId,
                summary.getActivityDate(),
                summary.getLastSyncedAt(),
                progress.getMetricType().name(),
                progress.getShortageValue().intValue()
        )));
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