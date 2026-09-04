package com.workoutdone.rpgym.game.character.adapter.in.web.dto;

import com.workoutdone.rpgym.game.character.application.CharacterView;

import java.time.Instant;
import java.util.UUID;

//캐릭터 조회 응답.
public record CharacterResponse (
        UUID userId,
        int level,
        String tier,
        int totalXp,
        int currentLevelXp,
        int xpForNextLevel,
        double progressPercent,
        Instant createdAt,
        Instant updatedAt
){

    public static CharacterResponse from(CharacterView view){
        return new CharacterResponse(
                view.userId(),
                view.level(),
                view.tier().name(),
                view.totalXp(),
                view.currentLevelXp(),
                view.xpForNextLevel(),
                view.progressPercent(),
                view.createdAt(),
                view.updatedAt()
        );
    }
}
