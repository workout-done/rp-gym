package com.workoutdone.rpgym.game.ranking.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RankingScoreTest {

    @DisplayName("level과 totalXp를 합성하고 그대로 되풀 수 있다.")
    @Test
    void encodeAndDecode(){
        double score = RankingScore.encode(12, 2450);

        assertThat(score).isEqualTo(12_000_002_450.0);
        assertThat(RankingScore.decodeLevel(score)).isEqualTo(12);
        assertThat(RankingScore.decodeXp(score)).isEqualTo(2450);
    }


    @DisplayName("레벨이 높으면 XP가 적어도 점수가 높다. - 2단 정렬이 유지되는지.")
    @Test
    void levelDominatesXp(){
        assertThat(RankingScore.encode(12, 0))
                .isGreaterThan(RankingScore.encode(11, 999_999_999));
    }


    @DisplayName("누적 XP가 배수 상한을 넘으면 조용히 틀어지지 않고 예외를 던진다.")
    @Test
    void rejectsXpOverLimit(){
        assertThatThrownBy(() -> RankingScore.encode(1, 1_000_000_000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("배수 상한");
    }





}
