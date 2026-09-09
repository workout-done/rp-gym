package com.workoutdone.rpgym.game.ranking.adapter.in.web.dto;

import com.workoutdone.rpgym.game.ranking.application.RankingEntryView;

import java.util.UUID;

public record RankingEntryResponse(
        long rank,
        UUID userId,
        int level,
        int totalXp,
        String tier
) {

    public static RankingEntryResponse from(RankingEntryView view) {
        return new RankingEntryResponse(
                view.rank(),
                view.userId(),
                view.level(),
                view.totalXp(),
                view.tier().name()
        );
    }
}
