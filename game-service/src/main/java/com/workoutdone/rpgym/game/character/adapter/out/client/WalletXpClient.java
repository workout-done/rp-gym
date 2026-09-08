package com.workoutdone.rpgym.game.character.adapter.out.client;

import com.workoutdone.rpgym.game.character.domain.XpClient;
import com.workoutdone.rpgym.game.xp.application.XpQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 실제 XP 원본(wallets) 어댑터.
 *
 * <p>{@code WalletRepository} 를 직접 쓰지 않고 {@link XpQueryService} 를 거친다.
 * wallets 는 XP 컨텍스트 소유라, 그쪽이 공개한 유스케이스로만 접근한다.
 * 그래야 wallets 의 내부 구조가 바뀌어도 이 어댑터가 깨지지 않는다.
 *
 * <p>{@code rpgym.xp.source} 로 MockXpClient 와 교체된다. 둘 중 하나만 빈으로 등록된다.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rpgym.xp.source", havingValue = "wallet")
public class WalletXpClient implements XpClient {

    private final XpQueryService xpQueryService;

    @Override
    public int findTotalXp(UUID userId) {
        return xpQueryService.totalXp(userId);
    }
}
