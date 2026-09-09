package com.workoutdone.rpgym.game.ranking.adapter.in.web;

import com.workoutdone.rpgym.game.ranking.adapter.in.web.dto.MyRankingResponse;
import com.workoutdone.rpgym.game.ranking.adapter.in.web.dto.RankingPageResponse;
import com.workoutdone.rpgym.game.ranking.application.RankingQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


//랭킹 조회 API
//인증 주체는 게이트웨이가 넣어주는 X-User-Id 헤더에서 얻는다.
@RestController
@RequestMapping("/api/v1/games/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingQueryUseCase rankingQueryUseCase;

    //전체 랭킹. 비어있어도 404가 아니라 빈 목록 200
    @GetMapping
    public ResponseEntity<RankingPageResponse> getRankings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        return ResponseEntity.ok(
                RankingPageResponse.from(rankingQueryUseCase.getRankings(page, size)));
    }

    // 내 랭킹. 집계 대상이 아니어도 404 가 아니라 rank: null 로 200 이다.
    @GetMapping("/me")
    public ResponseEntity<MyRankingResponse> getMyRanking(
            @RequestHeader("X-User-Id") UUID userId
            ){
        return ResponseEntity.ok(
                MyRankingResponse.from(rankingQueryUseCase.getMyRanking(userId))
        );
    }

}
