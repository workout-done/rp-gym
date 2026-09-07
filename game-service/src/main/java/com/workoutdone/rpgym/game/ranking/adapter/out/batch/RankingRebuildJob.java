package com.workoutdone.rpgym.game.ranking.adapter.out.batch;


import com.workoutdone.rpgym.game.character.adapter.out.persistence.CharacterRepository;
import com.workoutdone.rpgym.game.character.domain.Character;
import com.workoutdone.rpgym.game.character.domain.XpClient;
import com.workoutdone.rpgym.game.ranking.domain.RankingScore;
import com.workoutdone.rpgym.game.ranking.domain.RankingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

//Redis 유실 시 ZSET을 다시 만든다.
//정기 배치가 아님. Redis 장애 복구 직후 수동으로 한번 실행함.
@Slf4j
@Component
@RequiredArgsConstructor
public class RankingRebuildJob {

    //한번에 Redis로 보낼 건수.
    //Redis는 단일스레드라서 2.5만건을 한번에 보내면 그 명령을 처리하는 동안 다른요청이 전부 막히기에 끊어서 보냄.
    private static final int BATCH_SIZE = 1000;

    private final CharacterRepository characterRepository;
    private final XpClient xpClient;
    private final RankingStore rankingStore;



    @Transactional(readOnly = true)
    public long rebuild(){
        long total = 0;
        int pageNumber = 0;
        Page<Character> page;

        do {
            page = characterRepository.findAll(PageRequest.of(pageNumber, BATCH_SIZE));

            Map<UUID, Double> batch = new LinkedHashMap<>();
            for (Character character : page.getContent()){
                UUID userId = character.getUserId();
                int totalXp = xpClient.findTotalXp(userId);
                batch.put(userId, RankingScore.encode(character.getLevel(), totalXp));
            }
            rankingStore.saveAll(batch);

            total += batch.size();
            pageNumber++;
        }while(page.hasNext());

        log.info("랭킹 재구축 완료. 복구 건수={}", total);
        return total;
    }

}
/**
 * 이 코드는 wallets이 생기면 SQL 한번으로 바꿔야함.
 * 사용자마다 XpClient 를 호출하는 형태로 두었다 — 지금 MockXpClient 로 동작하고,
 * WalletXpClient 주석을 풀면 그대로 실제 값으로 바뀐다. 다만 N 번 호출이라 느리다.
 *
 * SELECT c.user_id, c.level, COALESCE(w.xp, 0) AS total_xp
 *   FROM characters c
 *   LEFT JOIN wallets w ON w.user_id = c.user_id
 *
 * LEFT JOIN 이 필수다. wallets 행은 최초 XP 지급 시에 생기므로,
 * INNER JOIN 이면 아직 XP 를 받지 못한 신규 사용자가 통째로 누락된다.
 */