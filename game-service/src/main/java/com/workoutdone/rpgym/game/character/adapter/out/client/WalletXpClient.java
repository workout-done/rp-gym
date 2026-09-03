package com.workoutdone.rpgym.game.character.adapter.out.client;

import com.workoutdone.rpgym.game.character.domain.XpClient;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

//MOCK을 걷어낼때 주석만 풀면됨.
// @Component("walletXpClient")
@RequiredArgsConstructor
public class WalletXpClient implements XpClient {

    // private final WalletRepository walletRepository;

    @Override
    public int findTotalXp(UUID userId) {
        // return walletRepository.findByUserId(userId)
        //         .map(Wallet::getXp)
        //         .orElse(0);
        throw new UnsupportedOperationException("wallets 구현 대기 중 (SA문서_2 5장)");
    }
}