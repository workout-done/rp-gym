package com.workoutdone.rpgym.game.xp.adapter.out.persistence;

import com.workoutdone.rpgym.game.xp.domain.aggregate.Wallet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WalletJpaRepository extends JpaRepository<Wallet, UUID> {
}
