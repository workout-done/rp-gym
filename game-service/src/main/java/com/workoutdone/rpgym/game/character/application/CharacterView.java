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
     * 캐릭터 행이 없는 경우. characters 행이 만들어지지 않은 사용자다.
     * 저장된 level 이 없으므로 XP 에서 계산한다. 하드코딩 1 보다 정확하고, 훅이 늦게 돌아도 자가 치유된다.
     * DB 에 행을 만들지는 않는다. GET 이 데이터를 바꾸면 안 되기 때문이다.
     */
    public static CharacterView empty(UUID userId, int totalXp) {
        return build(userId, LevelPolicy.levelOf(totalXp), totalXp, null, null);
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
