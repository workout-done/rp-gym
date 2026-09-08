package com.workoutdone.rpgym.game.xp.domain.aggregate;

import com.workoutdone.rpgym.common.entity.BaseCreatedEntity;
import com.workoutdone.rpgym.game.xp.domain.SourceType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Table(name = "xp_ledgers", schema = "game_service")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class XpLedger extends BaseCreatedEntity {

    @Id
    @Column(name = "ledger_id", nullable = false, updatable = false)
    private UUID ledgerId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20, updatable = false)
    private SourceType sourceType;

    @Column(name = "source_id", nullable = false, updatable = false)
    private UUID sourceId;

    @Column(name = "amount", nullable = false, updatable = false)
    private int amount;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    public static XpLedger create(
            UUID ledgerId,
            UUID userId,
            SourceType sourceType,
            UUID sourceId,
            int amount,
            Instant occurredAt
    ) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive but was " + amount);
        }

        XpLedger ledger = new XpLedger();
        ledger.ledgerId = ledgerId;
        ledger.userId = userId;
        ledger.sourceType = sourceType;
        ledger.sourceId = sourceId;
        ledger.amount = amount;
        ledger.occurredAt = occurredAt;
        return ledger;
    }
}
