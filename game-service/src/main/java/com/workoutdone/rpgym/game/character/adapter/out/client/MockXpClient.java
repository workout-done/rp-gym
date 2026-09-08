package com.workoutdone.rpgym.game.character.adapter.out.client;

import com.workoutdone.rpgym.game.character.domain.XpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(name = "rpgym.xp.source", havingValue = "mock", matchIfMissing = true)
public class MockXpClient implements XpClient {

    private static final int MAX_MOCK_XP = 5_000;

    @Override
    public int findTotalXp(UUID userId){
        int xp = Math.floorMod(userId.hashCode(), MAX_MOCK_XP);
        log.debug("[MOCK] XP 조회 userId={} totalXp={}", userId, xp);
        return xp;
    }
}
