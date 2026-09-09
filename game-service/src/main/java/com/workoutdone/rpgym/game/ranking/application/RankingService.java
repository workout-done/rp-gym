package com.workoutdone.rpgym.game.ranking.application;

import java.util.UUID;


//XP 담당이 호출할 랭킹 갱신 진입점.
public interface RankingService {
    //xp가 변경된 사용자의 파생 상태와 랭킹 점수를 갱신함.




    //멱등성. 같은 userId로 여러번 호출해도 결과가 같음.
    //매번 누적xp라는 절대값을 다시 읽어 통쨰로 덮어씀.
    void onXpChanged(UUID userId);
}
