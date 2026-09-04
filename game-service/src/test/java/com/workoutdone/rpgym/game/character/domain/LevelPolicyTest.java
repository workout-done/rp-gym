package com.workoutdone.rpgym.game.character.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class LevelPolicyTest {

    @DisplayName("누적 XP 로 레벨이 계산된다")
    @ParameterizedTest
    @CsvSource({"0, 1", "99, 1", "100, 2", "820, 9", "2450, 25"})
    void levelOf(int totalXp, int expectedLevel) {
        assertThat(LevelPolicy.levelOf(totalXp)).isEqualTo(expectedLevel);
    }

    @DisplayName("레벨 구간 진행도가 계산된다")
    @Test
    void progress() {
        assertThat(LevelPolicy.currentLevelXp(820)).isEqualTo(20);
        assertThat(LevelPolicy.xpForNextLevel(820)).isEqualTo(100);
        assertThat(LevelPolicy.progressPercent(820)).isEqualTo(20.0);
    }

    @DisplayName("레벨은 누적 XP 에 대해 단조 증가한다 — 랭킹 score 합성의 전제")
    @Test
    void monotonic() {
        int previous = LevelPolicy.levelOf(0);
        for (int xp = 1; xp <= 10_000; xp++) {
            int current = LevelPolicy.levelOf(xp);
            assertThat(current).isGreaterThanOrEqualTo(previous);
            previous = current;
        }
    }
}