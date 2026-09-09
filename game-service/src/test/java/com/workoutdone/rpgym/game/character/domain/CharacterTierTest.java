package com.workoutdone.rpgym.game.character.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CharacterTierTest {

    @DisplayName("레벨 구간마다 올바른 티어가 나온다")
    @ParameterizedTest
    @CsvSource({
            "1, BRONZE", "9, BRONZE",
            "10, SILVER", "19, SILVER",
            "20, GOLD", "29, GOLD",
            "30, PLATINUM", "39, PLATINUM",
            "40, DIAMOND", "999, DIAMOND"
    })
    void tierOf(int level, CharacterTier expected) {
        assertThat(CharacterTier.of(level)).isEqualTo(expected);
    }

    @DisplayName("레벨 1 미만은 존재할 수 없다")
    @ParameterizedTest
    @CsvSource({"0", "-1"})
    void invalidLevel(int level) {
        assertThatThrownBy(() -> CharacterTier.of(level))
                .isInstanceOf(IllegalArgumentException.class);
    }
}