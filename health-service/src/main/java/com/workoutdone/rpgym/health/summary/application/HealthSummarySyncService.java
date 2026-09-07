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
            boolean newlyAchieved = summary.markAllGoalsAchieved(now);
            summaryRepository.save(summary);
            // newlyAchieved == true일 때만 DailyGoalCompleted를 발행해야 중복 발행이 안 됨.
            // 이번 스코프는 발행 자체를 안 함(팀 결정: 보너스 XP 없음, Achievement는 추가기능 기간).
            // 나중에 이벤트 발행을 추가할 땐 이 변수를 조건으로 써야 한다.
        }
    }

    private DailyHealthSummary createInitialSummaryAndProgress(UUID userId, LocalDate activityDate) {
        DailyHealthSummary summary = DailyHealthSummary.createFor(userId, activityDate, LocalDateTime.now());
        summaryRepository.save(summary);

        // TODO: User Service health-contexts 조회로 실제 목표값 받아오기 (지금은 임시 기본값)
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