package com.workoutdone.rpgym.game.xp.domain.repo;

import com.workoutdone.rpgym.game.xp.domain.aggregate.XpLedger;

public interface XpLedgerRepository {
	XpLedger save(XpLedger ledger);
}
