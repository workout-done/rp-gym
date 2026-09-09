package com.workoutdone.rpgym.game.ranking.domain;

import java.util.UUID;

// 캐릭터 쓰기 아웃바운드 포트
//랭킹 조회는 DB를 읽지 않음. 갱신 경로는 characters.level 을 남김.
//Redis가 유실되었을 때 재구축의 근거가 되는것이 이 컬럼.
public interface CharacterLevelWriter {

    //캐릭터가 없으면 만들고, 있으면 레벨만 갱신함.
    //티어는 저장안함. 레벨에서 계산하는 값임.
    void upsertLevel(UUID userId, int level);
}
