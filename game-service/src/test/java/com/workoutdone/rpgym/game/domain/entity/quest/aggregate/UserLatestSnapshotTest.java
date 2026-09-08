package com.workoutdone.rpgym.game.domain.entity.quest.aggregate;

import com.workoutdone.rpgym.game.quest.domain.Metric;
import com.workoutdone.rpgym.game.quest.domain.aggregate.UserLatestSnapshot;
import com.workoutdone.rpgym.game.quest.domain.vo.Snapshot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserLatestSnapshotTest {

    private static final LocalDate DATE = LocalDate.of(2026, 8, 28);
    private static final Instant AT_10_30 = Instant.parse("2026-08-28T01:30:00Z");

    private Snapshot snapshot(String utcInstant, int steps) {
        return new Snapshot(DATE, Instant.parse(utcInstant), steps, 0, 0);
    }

    private UserLatestSnapshot stored() {
        return UserLatestSnapshot.create(UUID.randomUUID(), new Snapshot(DATE, AT_10_30, 3000, 31, 155));
    }

    @Test
    @DisplayName("더 뒤의 measuredAt이면 덮어쓴다")
    void 더_뒤의_스냅샷이면_덮어쓴다() {
        UserLatestSnapshot latest = stored();

        boolean applied = latest.applyIfNewer(snapshot("2026-08-28T02:00:00Z", 3500));

        assertTrue(applied);
        assertEquals(Instant.parse("2026-08-28T02:00:00Z"), latest.getMeasuredAt());
        assertEquals(3500, latest.getSteps());
    }

    @Test
    @DisplayName("같은 measuredAt이면 덮어쓰지 않는다")
    void 같은_measuredAt이면_덮어쓰지_않는다() {
        UserLatestSnapshot latest = stored();

        boolean applied = latest.applyIfNewer(snapshot("2026-08-28T01:30:00Z", 9999));

        assertFalse(applied);
        assertEquals(3000, latest.getSteps());
    }

    @Test
    @DisplayName("더 오래된 measuredAt이면 덮어쓰지 않는다 — 과거가 최신을 덮으면 baseline이 소급된다")
    void 더_오래된_스냅샷이면_덮어쓰지_않는다() {
        UserLatestSnapshot latest = stored();

        boolean applied = latest.applyIfNewer(snapshot("2026-08-28T01:00:00Z", 1200));

        assertFalse(applied);
        assertEquals(AT_10_30, latest.getMeasuredAt());
        assertEquals(3000, latest.getSteps());
    }

    @Test
    @DisplayName("toSnapshot으로 baseline 값을 꺼낼 수 있다")
    void toSnapshot으로_baseline을_꺼낸다() {
        UserLatestSnapshot latest = stored();

        Snapshot restored = latest.toSnapshot();

        assertEquals(AT_10_30, restored.measuredAt());
        assertEquals(31, restored.valueOf(Metric.ACTIVE_MINUTES));
        assertEquals(3000, restored.valueOf(Metric.STEPS));
    }
}
