package com.workoutdone.rpgym.game.xp.domain.aggregate;

import com.workoutdone.rpgym.common.entity.BaseCreatedUpdatedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@Table(name = "wallets", schema = "game_service")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet extends BaseCreatedUpdatedEntity {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "xp", nullable = false)
    private int xp;

    public static Wallet create(UUID userId) {
        Wallet wallet = new Wallet();
        wallet.userId = userId;
        wallet.xp = 0;
        return wallet;
    }

    public void addXp(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive but was " + amount);
        }
        this.xp += amount;
    }
}
