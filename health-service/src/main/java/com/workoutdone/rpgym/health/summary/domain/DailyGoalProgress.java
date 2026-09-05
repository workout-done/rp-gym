package com.workoutdone.rpgym.health.summary.domain;

import com.workoutdone.rpgym.common.entity.BaseCreatedUpdatedEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "daily_goal_progress",
        schema = "health_service",
        uniqueConstraints = @UniqueConstraint(
                name = "ux_daily_goal_progress_summary_metric",
                columnNames = {"summary_id", "metric_type"}
        )
)
public class DailyGoalProgress extends BaseCreatedUpdatedEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "progress_id", updatable = false, nullable = false)
    private UUID progressId;

    @Column(name = "summary_id", updatable = false, nullable = false)
    private UUID summaryId;

    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    @Column(name = "activity_date", updatable = false, nullable = false)
    private LocalDate activityDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", updatable = false, nullable = false, length = 30)
    private MetricType metricType;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "targetValue", column = @Column(name = "target_value")),
            @AttributeOverride(name = "achievedValue", column = @Column(name = "achieved_value")),
            @AttributeOverride(name = "shortageValue", column = @Column(name = "shortage_value")),
            @AttributeOverride(name = "achieved", column = @Column(name = "is_achieved")),
            @AttributeOverride(name = "unit", column = @Column(name = "unit"))
    })
    private GoalMetricValue goalMetricValue;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    private DailyGoalProgress(UUID summaryId, UUID userId, LocalDate activityDate,
                              MetricType metricType, GoalMetricValue goalMetricValue) {
        this.summaryId = summaryId;
        this.userId = userId;
        this.activityDate = activityDate;
        this.metricType = metricType;
        this.goalMetricValue = goalMetricValue;
    }

    public static DailyGoalProgress createFor(UUID summaryId, UUID userId, LocalDate activityDate,
                                              MetricType metricType, BigDecimal targetValue) {
        GoalMetricValue initial = GoalMetricValue.of(targetValue, BigDecimal.ZERO, metricType);
        return new DailyGoalProgress(summaryId, userId, activityDate, metricType, initial);
    }

    public void updateAchieved(BigDecimal newAchievedValue) {
        this.goalMetricValue = this.goalMetricValue.withAchieved(newAchievedValue);
    }

    public BigDecimal getShortageValue() {
        return goalMetricValue.getShortageValue();
    }

    public boolean isAchieved() {
        return goalMetricValue.isAchieved();
    }
}