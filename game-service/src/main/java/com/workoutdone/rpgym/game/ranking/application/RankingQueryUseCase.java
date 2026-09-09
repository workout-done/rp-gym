package com.workoutdone.rpgym.game.ranking.application;

import java.util.UUID;

public interface RankingQueryUseCase {

    // 전체 랭킹을 페이지단위로 조회.
    // param page 0 이상
    // param size 1 ~ 100
    RankingPageView getRankings(int page, int size);

    //내 랭킹, 집계 대상이 아니어도 예외를 던지지않고 기본값으로 채워 도려줌
    MyRankingView getMyRanking(UUID userId);
}
