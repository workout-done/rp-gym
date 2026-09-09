package com.workoutdone.rpgym.game.xp.application;

import com.workoutdone.rpgym.game.xp.domain.aggregate.Wallet;
import com.workoutdone.rpgym.game.xp.domain.repo.WalletRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class XpQueryService {

    private final WalletRepository walletRepository;

    @Transactional(readOnly = true)
    public int totalXp(UUID userId) {
        return walletRepository.findByUserId(userId)
                .map(Wallet::getXp)
                .orElse(0);
    }
}
