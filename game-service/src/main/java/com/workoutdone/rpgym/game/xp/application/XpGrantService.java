package com.workoutdone.rpgym.game.xp.application;

import com.workoutdone.rpgym.game.xp.domain.SourceType;
import com.workoutdone.rpgym.game.xp.domain.aggregate.Wallet;
import com.workoutdone.rpgym.game.xp.domain.aggregate.XpLedger;
import com.workoutdone.rpgym.game.xp.domain.repo.WalletRepository;
import com.workoutdone.rpgym.game.xp.domain.repo.XpLedgerRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class XpGrantService {

    private final XpLedgerRepository xpLedgerRepository;
    private final WalletRepository walletRepository;

    @Transactional
    public XpLedger grant(UUID userId, SourceType sourceType, UUID sourceId, int amount, Instant occurredAt) {
        XpLedger ledger = xpLedgerRepository.save(
                XpLedger.create(UUID.randomUUID(), userId, sourceType, sourceId, amount, occurredAt));

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseGet(() -> Wallet.create(userId));
        wallet.addXp(amount);
        walletRepository.save(wallet);

        return ledger;
    }
}
