package com.workoutdone.rpgym.game.ranking.application;

import com.workoutdone.rpgym.game.character.domain.CharacterTier;
import com.workoutdone.rpgym.game.ranking.domain.RankingScore;

import java.util.UUID;

public record RankingEntryView(
        long rank,
        UUID userId,
        int level,
        int totalXp,
        CharacterTier tier
) {
    public static RankingEntryView of(long rank, UUID userId, double score){
        int level = RankingScore.decodeLevel(score);
        return new RankingEntryView(
                rank,
                userId,
                level,
                RankingScore.decodeXp(score),
                CharacterTier.of(level)
        );
    }
}
