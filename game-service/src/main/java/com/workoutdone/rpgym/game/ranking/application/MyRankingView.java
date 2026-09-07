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

    // 아직 XP를 한번도 받지못해 ZSET에 없는 사용자
    //404가 아님. 기본값 200으로 캐릭터 조회와 같은 원칙으로 응답함.
    public static MyRankingView notRanked(UUID userId, long totalCount){
        return new MyRankingView(
                null,
                totalCount,
                null,
                userId,
                LevelPolicy.INITIAL_LEVEL,
                0,
                CharacterTier.of(LevelPolicy.INITIAL_LEVEL)
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
