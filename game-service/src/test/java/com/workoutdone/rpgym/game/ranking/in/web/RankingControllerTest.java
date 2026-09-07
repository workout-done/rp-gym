package com.workoutdone.rpgym.game.ranking.in.web;

import com.workoutdone.rpgym.common.exception.GlobalExceptionHandler;
import com.workoutdone.rpgym.game.character.domain.CharacterTier;
import com.workoutdone.rpgym.game.character.exception.GameExceptionHandler;
import com.workoutdone.rpgym.game.ranking.adapter.in.web.RankingController;
import com.workoutdone.rpgym.game.ranking.application.MyRankingView;
import com.workoutdone.rpgym.game.ranking.application.RankingEntryView;
import com.workoutdone.rpgym.game.ranking.application.RankingPageView;
import com.workoutdone.rpgym.game.ranking.application.RankingQueryUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RankingController.class)
@Import({GameExceptionHandler.class, GlobalExceptionHandler.class})
class RankingControllerTest {

    private static final String RANKINGS_URL = "/api/v1/games/rankings";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RankingQueryUseCase rankingQueryUseCase;

    @DisplayName("전체 랭킹을 200 으로 응답한다")
    @Test
    void getRankings() throws Exception {
        UUID userId = UUID.randomUUID();
        given(rankingQueryUseCase.getRankings(anyInt(), anyInt())).willReturn(
                new RankingPageView(
                        List.of(new RankingEntryView(1L, userId, 12, 2450, CharacterTier.SILVER)),
                        0, 20, 1));

        mockMvc.perform(get(RANKINGS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].rank").value(1))
                .andExpect(jsonPath("$.content[0].level").value(12))
                .andExpect(jsonPath("$.content[0].tier").value("SILVER"))
                .andExpect(jsonPath("$.page").value(0));
    }

    @DisplayName("size 가 허용 범위를 벗어나면 400 INVALID_INPUT 을 반환한다")
    @Test
    void invalidSize() throws Exception {
        given(rankingQueryUseCase.getRankings(anyInt(), anyInt()))
                .willThrow(new IllegalArgumentException("size 는 1 이상 100 이하여야 합니다: 101"));

        mockMvc.perform(get(RANKINGS_URL).param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @DisplayName("X-User-Id 헤더가 없으면 401 UNAUTHORIZED 를 반환한다")
    @Test
    void missingUserIdHeader() throws Exception {
        mockMvc.perform(get(RANKINGS_URL + "/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @DisplayName("집계 대상이 아닌 사용자도 200 으로 응답하고 rank 는 null 이다")
    @Test
    void notRankedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        given(rankingQueryUseCase.getMyRanking(any()))
                .willReturn(MyRankingView.notRanked(userId, 137));

        mockMvc.perform(get(RANKINGS_URL + "/me").header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rank").doesNotExist())
                .andExpect(jsonPath("$.level").value(1))
                .andExpect(jsonPath("$.tier").value("BRONZE"))
                .andExpect(jsonPath("$.totalCount").value(137));
    }
}
