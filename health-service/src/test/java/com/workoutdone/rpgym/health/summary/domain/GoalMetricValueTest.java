package com.workoutdone.rpgym.health.summary.domain;

import com.workoutdone.rpgym.common.exception.BaseException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoalMetricValueTest {

    @Test
    void 목표값이_음수이면_예외가_발생한다() {
        assertThatThrownBy(() ->
                GoalMetricValue.of(BigDecimal.valueOf(-1), BigDecimal.ZERO, MetricType.STEPS)
        ).isInstanceOf(BaseException.class);
    }

    @Test
    void 달성값이_음수이면_예외가_발생한다() {
        assertThatThrownBy(() ->
                GoalMetricValue.of(BigDecimal.valueOf(5000), BigDecimal.valueOf(-1), MetricType.STEPS)
        ).isInstanceOf(BaseException.class);
    }

    @Test
    void 부족량은_목표에서_달성값을_뺀_값이다() {
        GoalMetricValue value = GoalMetricValue.of(
                BigDecimal.valueOf(5000), BigDecimal.valueOf(3200), MetricType.STEPS
        );

        assertThat(value.getShortageValue()).isEqualByComparingTo(BigDecimal.valueOf(1800));
        assertThat(value.isAchieved()).isFalse();
    }

    @Test
    void 달성값이_목표를_넘으면_부족량은_0이고_달성_상태다() {
        GoalMetricValue value = GoalMetricValue.of(
                BigDecimal.valueOf(5000), BigDecimal.valueOf(5300), MetricType.STEPS
        );

        assertThat(value.getShortageValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(value.isAchieved()).isTrue();
    }

    @Test
    void withAchieved는_target과_unit을_유지한채_달성값만_갱신한다() {
        GoalMetricValue original = GoalMetricValue.of(
                BigDecimal.valueOf(5000), BigDecimal.valueOf(3200), MetricType.STEPS
        );

        GoalMetricValue updated = original.withAchieved(BigDecimal.valueOf(5300));

        assertThat(updated.isAchieved()).isTrue();
        assertThat(updated.getShortageValue()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}