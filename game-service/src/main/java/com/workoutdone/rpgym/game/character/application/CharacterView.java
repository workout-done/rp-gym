package com.workoutdone.rpgym.game.character.application;

import com.workoutdone.rpgym.game.character.domain.Character;
import com.workoutdone.rpgym.game.character.domain.CharacterTier;
import com.workoutdone.rpgym.game.character.domain.LevelPolicy;

import java.time.Instant;
import java.util.UUID;


//캐릭터 조회 결과 모델.
public record CharacterView (
  UUID userId,
  int level,
  CharacterTier tier,
  int totalXp,
  int currentLevelXp,
  int xpForNextLevel,
  double progressPercent,
  Instant createdAt,
  Instant updatedAt
){

    /** 캐릭터 행이 있는 경우. level 은 저장된 값을 신뢰한다. */
    public static CharacterView of(Character character, int totalXp) {
        return build(
                character.getUserId(),
                character.getLevel(),
                totalXp,
                character.getCreatedAt(),
                character.getUpdatedAt()
        );
    }

    /**
     * 캐릭터 행이 없는 경우.
     *
     * <p>XP 를 한 번도 받지 못해 {@code onXpChanged()} 훅이 돈 적 없는 사용자다.
     * DB 를 건드리지 않고 기본값으로 조립한다 (SA문서_2 10.1).
     */
    public static CharacterView empty(UUID userId) {
        return build(userId, LevelPolicy.INITIAL_LEVEL, 0, null, null);
    }

    private static CharacterView build(UUID userId,
                                       int level,
                                       int totalXp,
                                       Instant createdAt,
                                       Instant updatedAt) {
        return new CharacterView(
                userId,
                level,
                CharacterTier.of(level),
                totalXp,
                LevelPolicy.currentLevelXp(totalXp),
                LevelPolicy.xpForNextLevel(totalXp),
                LevelPolicy.progressPercent(totalXp),
                createdAt,
                updatedAt
        );
    }
}
