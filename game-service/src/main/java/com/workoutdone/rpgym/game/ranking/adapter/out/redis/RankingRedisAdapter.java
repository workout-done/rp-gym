package com.workoutdone.rpgym.game.ranking.adapter.out.redis;

import com.workoutdone.rpgym.game.ranking.domain.RankingStore;
import com.workoutdone.rpgym.game.ranking.domain.ScoredMember;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class RankingRedisAdapter implements RankingStore {

    private static final String KEY = "ranking:global";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(UUID userId, double score){
        zset().add(KEY, userId.toString(), score);
    }

    @Override
    public void saveAll(Map<UUID, Double> scores){
        if (scores.isEmpty()){
            return;
        }
        Set<TypedTuple<String>> tuples = new LinkedHashSet<>();
        scores.forEach((userId, score) ->
                tuples.add(TypedTuple.of(userId.toString(), score)));
        zset().add(KEY, tuples);
    }

    @Override
    public Optional<Double> findScore(UUID userId){
        return Optional.ofNullable(zset().score(KEY, userId.toString()));
    }

    @Override
    public long countHigherThan(double score){
        //ZREVRANK를 쓰지않는다.
        //정렬된 인덱스를 주기 때문에 동점자가 15등 16등으로 갈리고
        //동점자에게 같은 순위를 주는 전체 랭킹결과가 어늣난다.
        //score가 항상 정수라 하한을 score + 1로 주면 "나보다 높은사람"을 정확히 셀수있기때문.

        return nvl(zset().count(KEY, score + 1, Double.POSITIVE_INFINITY));
    }

    @Override
    public long size(){
        return nvl(zset().zCard(KEY));
    }

    @Override
    public List<ScoredMember> findPage(long start, long end){
        Set<TypedTuple<String>> tuples = zset().reverseRangeWithScores(KEY, start, end);
        if (tuples == null || tuples.isEmpty()){
            return List.of();
        }
        List<ScoredMember> result = new ArrayList<>(tuples.size());
        for (TypedTuple<String> tuple : tuples){
            if (tuple.getValue() == null || tuple.getScore() == null){
                continue;
            }
            result.add(new ScoredMember(UUID.fromString(tuple.getValue()), tuple.getScore()));
        }
        return result;
    }


    private ZSetOperations<String, String> zset(){
        return redisTemplate.opsForZSet();
    }

    private long nvl(Long value){
        return value == null ? 0L : value;
    }
}
