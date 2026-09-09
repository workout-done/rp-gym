package com.workoutdone.rpgym.game.ranking.domain;

import java.util.UUID;

//ZSET에서 꺼낸 한 사람. member와 score의 쌍.
public record ScoredMember (UUID userId, double score){
}
//레벨(level), 누적xp(totalXp), tier(티어)는 전부 score에서 파생됨 별도 필드 안둠.