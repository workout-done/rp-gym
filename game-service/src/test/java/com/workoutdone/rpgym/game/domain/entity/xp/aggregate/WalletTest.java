package com.workoutdone.rpgym.game.domain.entity.xp.aggregate;

import com.workoutdone.rpgym.game.xp.domain.aggregate.Wallet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WalletTest {

    @Test
    @DisplayName("생성 직후 XP는 0이다")
    void 생성_직후_XP는_0이다() {
        assertEquals(0, Wallet.create(UUID.randomUUID()).getXp());
    }

    @Test
    @DisplayName("addXp는 누적된다 — 원장 합계와 대조되는 집계값이다")
    void addXp는_누적된다() {
        Wallet wallet = Wallet.create(UUID.randomUUID());

        wallet.addXp(5);
        wallet.addXp(10);

        assertEquals(15, wallet.getXp());
    }

    @Test
    @DisplayName("0 이하의 XP는 지급할 수 없다")
    void 영_이하는_지급할_수_없다() {
        Wallet wallet = Wallet.create(UUID.randomUUID());

        assertThrows(IllegalArgumentException.class, () -> wallet.addXp(0));
        assertThrows(IllegalArgumentException.class, () -> wallet.addXp(-5));
    }
}
