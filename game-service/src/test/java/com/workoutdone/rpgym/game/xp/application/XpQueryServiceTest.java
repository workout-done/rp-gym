package com.workoutdone.rpgym.game.xp.application;

import com.workoutdone.rpgym.game.xp.domain.aggregate.Wallet;
import com.workoutdone.rpgym.game.xp.domain.repo.WalletRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class XpQueryServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();

    private WalletRepository walletRepository;
    private XpQueryService service;

    @BeforeEach
    void setUp() {
        walletRepository = mock(WalletRepository.class);
        service = new XpQueryService(walletRepository);
    }

    @Test
    @DisplayName("지갑의 집계값을 그대로 돌려준다")
    void 지갑의_집계값을_돌려준다() {
        Wallet wallet = Wallet.create(USER_ID);
        wallet.addXp(35);
        when(walletRepository.findByUserId(USER_ID)).thenReturn(Optional.of(wallet));

        assertEquals(35, service.totalXp(USER_ID));
    }

    @Test
    @DisplayName("지갑이 없으면 0이다 — 아직 XP를 받은 적 없는 유저")
    void 지갑이_없으면_0이다() {
        when(walletRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertEquals(0, service.totalXp(USER_ID));
    }
}
