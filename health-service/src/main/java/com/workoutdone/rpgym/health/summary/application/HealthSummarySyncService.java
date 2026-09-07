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

    @Transactional
    public void sync(UUID userId, UUID activityId, LocalDate activityDate,
                     int steps, int activeMinutes, int activeCalories, Instant measuredAt) {

        DailyHealthSummary summary = summaryRepository
                .findByUserIdAndActivityDate(userId, activityDate)
                .orElseGet(() -> createInitialSummaryAndProgress(userId, activityDate));

        LocalDateTime now = LocalDateTime.now();
        boolean applied = summary.applySync(steps, activeMinutes, activeCalories, measuredAt, now);
        if (!applied) {
            return; // 순서 역전된 오래된 데이터만 무시 (재동기화는 통과됨)
        }
        summaryRepository.save(summary);

        List<DailyGoalProgress> progresses = progressRepository.findBySummaryId(summary.getSummaryId());
        for (DailyGoalProgress progress : progresses) {
            BigDecimal achievedValue = resolveAchievedValue(progress.getMetricType(), steps, activeMinutes, activeCalories);
            progress.updateAchieved(achievedValue);
        }
        progressRepository.saveAll(progresses);

        boolean allAchieved = progresses.stream().allMatch(DailyGoalProgress::isAchieved);
        if (allAchieved) {
            summary.markAllGoalsAchieved(now);
            summaryRepository.save(summary);
        }
    }

    private DailyHealthSummary createInitialSummaryAndProgress(UUID userId, LocalDate activityDate) {
        DailyHealthSummary summary = DailyHealthSummary.createFor(userId, activityDate, LocalDateTime.now());
        summaryRepository.save(summary);

        // TODO: User Service health-contexts 조회로 실제 목표값 받아오기 (지금은 임시 기본값)
        progressRepository.saveAll(List.of(
                DailyGoalProgress.createFor(summary.getSummaryId(), userId, activityDate, MetricType.STEPS, BigDecimal.valueOf(3000)),
                DailyGoalProgress.createFor(summary.getSummaryId(), userId, activityDate, MetricType.ACTIVE_MINUTES, BigDecimal.valueOf(30)),
                DailyGoalProgress.createFor(summary.getSummaryId(), userId, activityDate, MetricType.ACTIVE_CALORIES, BigDecimal.valueOf(300))
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