package com.workoutdone.rpgym.game.ranking.adapter.in.web.dto;

import com.workoutdone.rpgym.game.ranking.application.RankingPageView;

import java.util.List;

/**
 * 전체 랭킹 응답.
 *
 * <p>page 는 0-based 다. common 의 {@code PageResponse} 와 기준이 다르므로 주의한다 (2-1 비고).
 */
public record RankingPageResponse(
        List<RankingEntryResponse> content,
        int page,
        int size,
        long totalElements
) {

    public static RankingPageResponse from(RankingPageView view) {
        return new RankingPageResponse(
                view.content().stream().map(RankingEntryResponse::from).toList(),
                view.page(),
                view.size(),
                view.totalElements()
        );
    }
}