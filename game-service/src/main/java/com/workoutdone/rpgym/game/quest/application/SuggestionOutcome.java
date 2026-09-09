package com.workoutdone.rpgym.game.quest.application;

public enum SuggestionOutcome {
    CREATED,
    DUPLICATE_SUGGESTION,
    ALREADY_ACTIVE,
    UNKNOWN_METRIC,
    INVALID_TARGET,
    SNAPSHOT_MISSING,
    SNAPSHOT_MISMATCH,
    DATE_MISMATCH
}
