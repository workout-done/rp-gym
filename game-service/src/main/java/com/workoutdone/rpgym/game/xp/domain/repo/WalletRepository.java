package com.workoutdone.rpgym.game.xp.domain.repo;

import java.util.Optional;
import java.util.UUID;

import com.workoutdone.rpgym.game.xp.domain.aggregate.Wallet;

public interface WalletRepository {
	Optional<Wallet> findByUserId(UUID userId);
	Wallet save(Wallet wallet);
}
