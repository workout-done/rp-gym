package com.workoutdone.rpgym.health.summary.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DailyHealthSummaryTest {

    @Test
    void 최초_생성시_전체달성여부는_false이고_achievedAt은_null이다() {
        DailyHealthSummary summary = DailyHealthSummary.createFor(
                UUID.randomUUID(), LocalDate.of(2026, 8, 30), LocalDateTime.now()
        );

        assertThat(summary.isAllGoalsAchieved()).isFalse();
        assertThat(summary.getAchievedAt()).isNull();
    }

    @Test
    void 최초로_모든목표를_달성하면_true를_반환하고_achievedAt이_설정된다() {
        DailyHealthSummary summary = DailyHealthSummary.createFor(
                UUID.randomUUID(), LocalDate.of(2026, 8, 30), LocalDateTime.now()
        );
        LocalDateTime achievedAt = LocalDateTime.of(2026, 8, 30, 21, 10, 0);

        boolean firstResult = summary.markAllGoalsAchieved(achievedAt);

        assertThat(firstResult).isTrue();
        assertThat(summary.isAllGoalsAchieved()).isTrue();
        assertThat(summary.getAchievedAt()).isEqualTo(achievedAt);
    }

    @Test
    void 이미_달성한_상태에서_다시_호출하면_false를_반환하고_achievedAt이_바뀌지_않는다() {
        DailyHealthSummary summary = DailyHealthSummary.createFor(
                UUID.randomUUID(), LocalDate.of(2026, 8, 30), LocalDateTime.now()
        );
        LocalDateTime firstAchievedAt = LocalDateTime.of(2026, 8, 30, 21, 10, 0);
        summary.markAllGoalsAchieved(firstAchievedAt);

        boolean secondResult = summary.markAllGoalsAchieved(LocalDateTime.of(2026, 8, 30, 22, 0, 0));

        assertThat(secondResult).isFalse();
        assertThat(summary.getAchievedAt()).isEqualTo(firstAchievedAt);
    }
}