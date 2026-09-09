package com.workoutdone.rpgym.game.ranking.application;

import com.workoutdone.rpgym.game.character.domain.CharacterTier;
import com.workoutdone.rpgym.game.character.domain.LevelPolicy;
import com.workoutdone.rpgym.game.ranking.domain.RankingScore;

import java.util.UUID;

//내 랭킹 결과
public record MyRankingView(
        Long rank,
        long totalCount,
        Double percentile,
        UUID userId,
        int level,
        int totalXp,
        CharacterTier tier
) {
    public static MyRankingView of(UUID userId, long rank, long totalCount, double score){
        int level = RankingScore.decodeLevel(score);
        return new MyRankingView(
                rank,
                totalCount,
                percentileOf(rank, totalCount),
                userId,
                level,
                RankingScore.decodeXp(score),
                CharacterTier.of(level)
        );
    }

    /** ZSET 에 아직 없는 사용자. 404 가 아니라 기본값 200 으로 응답한다. */
    public static MyRankingView notRanked(UUID userId, long totalCount, int totalXp){
        int level = LevelPolicy.levelOf(totalXp);
        return new MyRankingView(
                null,
                totalCount,
                null,
                userId,
                level,
                totalXp,
                CharacterTier.of(level)
        );
    }

    // 상위 백분위(%) 값이 작을수록 상위다. 소수 첫째 자리에서 반올림함.
    private static Double percentileOf(long rank, long totalCount){
        if (totalCount <= 0){
            return null;
        }
        return Math.round(rank * 1000.0 / totalCount) / 10.0;
    }
}
