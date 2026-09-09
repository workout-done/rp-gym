package com.workoutdone.rpgym.game.ranking.domain;

//랭킹 저장소 아웃바운드 포트. 구현은 Redis Sorted Set.

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface RankingStore {

    //점수를 덮어씀(ZADD) 같은 값으로 여러번 호출해도 결과가 같음.
    void save(UUID userId, double score);

    // 여러 건을 한번에 덮어씀. 재구축 배치에서 쓰임
    void saveAll(Map<UUID, Double> scores);

    // 내 점수. 아직 집계 대상이 아니면 빔.
    Optional<Double> findScore(UUID userId);

    //주어진 점수보다 높은 점수를 가진 수.
    //순위는 "나보다 높은 사람 + 1"로 계산함. 이렇게 해야 동점자가 같은 순위를 받음.
    long countHigherThan(double score);

    //전체 참여자 수.(ZCARD)
    long size();

    //점수 내림차순으로 start ~ end 구간을 가져옴. 둘다 0-based이고 끝을 포함함.
    //범위를 벗어나면 빈 목록임.
    List<ScoredMember> findPage(long start, long end);
}
