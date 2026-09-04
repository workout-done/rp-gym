package com.workoutdone.rpgym.health.summary.domain;

import com.workoutdone.rpgym.common.exception.BaseException;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class GoalMetricValue {

    protected GoalMetricValue() {
        this.targetValue = null;
        this.achievedValue = null;
        this.unit = null;
        this.shortageValue = null;
        this.achieved = false;
    }

    @Column(name = "target_value", nullable = false, precision = 10, scale = 2)
    private final BigDecimal targetValue;

    @Column(name = "achieved_value", nullable = false, precision = 10, scale = 2)
    private final BigDecimal achievedValue;

    @Column(name = "unit", nullable = false, length = 10)
    private final String unit;

    @Column(name = "shortage_value", nullable = false, precision = 10, scale = 2)
    private final BigDecimal shortageValue;

    @Column(name = "is_achieved", nullable = false)
    private final boolean achieved;

    private GoalMetricValue(BigDecimal targetValue, BigDecimal achievedValue, String unit) {
        this.targetValue = targetValue;
        this.achievedValue = achievedValue;
        this.unit = unit;
        this.shortageValue = targetValue.subtract(achievedValue).max(BigDecimal.ZERO);
        this.achieved = achievedValue.compareTo(targetValue) >= 0;
    }

    public static GoalMetricValue of(BigDecimal targetValue, BigDecimal achievedValue, MetricType metricType) {
        validateNotNegative(targetValue);
        validateNotNegative(achievedValue);
        return new GoalMetricValue(targetValue, achievedValue, metricType.getUnit());
    }

    private static void validateNotNegative(BigDecimal value) {
        if (value == null || value.signum() < 0) {
            throw new BaseException(SummaryErrorCode.INVALID_GOAL_METRIC_VALUE);
        }
    }

    public BigDecimal getShortageValue() {
        return shortageValue;
    }

    public boolean isAchieved() {
        return achieved;
    }

    public GoalMetricValue withAchieved(BigDecimal newAchievedValue) {
        validateNotNegative(newAchievedValue);
        return new GoalMetricValue(this.targetValue, newAchievedValue, this.unit);
    }
}