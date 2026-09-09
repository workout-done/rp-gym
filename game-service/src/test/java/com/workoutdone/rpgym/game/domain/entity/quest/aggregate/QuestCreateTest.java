package com.workoutdone.rpgym.game.domain.entity.quest.aggregate;

import com.workoutdone.rpgym.game.quest.domain.Metric;
import com.workoutdone.rpgym.game.quest.domain.QuestStatus;
import com.workoutdone.rpgym.game.quest.domain.aggregate.Quest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


class QuestCreateTest {

    private static final Instant BASELINE_AT = Instant.parse("2026-08-28T01:30:00Z"); // KST 10:30
    private static final Instant EXPIRES_AT = Instant.parse("2026-08-28T14:59:59Z");  // KST 23:59:59

    private Quest create(int targetVal, int baselineVal, Instant baselineMeasuredAt, Instant expiredAt) {
        return Quest.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "20분 산책하기", Metric.ACTIVE_MINUTES,
                targetVal, baselineVal, baselineMeasuredAt,
                5, expiredAt
        );
    }

    @Test
    @DisplayName("정상 값이면 ACTIVE로 생성되고 진행 이력은 비어 있다")
    void 정상_값이면_ACTIVE로_생성된다() {
        Quest quest = create(20, 31, BASELINE_AT, EXPIRES_AT);

        assertAll(
                () -> assertEquals(QuestStatus.ACTIVE, quest.getStatus()),
                () -> assertNull(quest.getLastCumulativeVal()),        // 첫 이벤트 전
                () -> assertNull(quest.getLastAppliedMeasuredAt()),    // null이어야 첫 스냅샷이 통과한다
                () -> assertEquals(31, quest.getBaselineVal())
        );
    }

    @Test
    @DisplayName("목표값이 0이면 생성되지 않는다 — 첫 스냅샷에서 즉시 완료되어 공짜 XP가 나간다")
    void 목표값이_0이면_생성되지_않는다() {
        assertThrows(IllegalArgumentException.class,
                () -> create(0, 31, BASELINE_AT, EXPIRES_AT));
    }

    @Test
    @DisplayName("목표값이 음수면 생성되지 않는다")
    void 목표값이_음수면_생성되지_않는다() {
        assertThrows(IllegalArgumentException.class,
                () -> create(-5, 31, BASELINE_AT, EXPIRES_AT));
    }

    @Test
    @DisplayName("baseline이 음수면 생성되지 않는다 — 달성분이 부풀려진다")
    void baseline이_음수면_생성되지_않는다() {
        assertThrows(IllegalArgumentException.class,
                () -> create(20, -1, BASELINE_AT, EXPIRES_AT));
    }

    @Test
    @DisplayName("만료 시각이 기준 시각보다 빠르면 생성되지 않는다 — 그 유저가 새 Quest를 영원히 못 받는다")
    void 만료_시각이_기준_시각보다_빠르면_생성되지_않는다() {
        assertThrows(IllegalArgumentException.class,
                () -> create(20, 31, EXPIRES_AT, BASELINE_AT)); // 두 시각을 뒤집어 넣음
    }
}
