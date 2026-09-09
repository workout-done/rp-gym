package com.workoutdone.rpgym.game.ranking.application;

import java.util.List;


//랭킹 페이지 결과
public record RankingPageView(
        List<RankingEntryView> content,
        int page,
        int size,
        long totalElements
) {
    public static RankingPageView empty(int page, int size, long totalElements){
        return new RankingPageView(List.of(), page, size, totalElements);
    }
}
