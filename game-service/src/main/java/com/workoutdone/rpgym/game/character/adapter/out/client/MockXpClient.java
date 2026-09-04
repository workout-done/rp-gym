package com.workoutdone.rpgym.game.character.adapter.out.client;

import com.workoutdone.rpgym.game.character.domain.XpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@ConditionalOnMissingBean(name = "walletXpClient")
public class MockXpClient implements XpClient {

    private static final int MAX_MOCK_XP = 5_000;

    @Override
    public int findTotalXp(UUID userId){
        int xp = Math.floorMod(userId.hashCode(), MAX_MOCK_XP);
        log.debug("[MOCK] XP 조회 userId={} totalXp={}", userId, xp);
        return xp;
    }
}
