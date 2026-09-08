package com.workoutdone.rpgym.game.quest.application.payload;

import com.workoutdone.rpgym.game.quest.domain.aggregate.Quest;

import java.time.Instant;
import java.util.UUID;

public record QuestCreatedData(
        UUID questId,
        UUID suggestionId,
        String title,
        String metric,
        int targetValue,
        int baselineValue,
        Instant baselineMeasuredAt,
        int rewardXp,
        Instant expiresAt
) {

    public static QuestCreatedData from(Quest quest) {
        return new QuestCreatedData(
                quest.getQuestId(),
                quest.getSuggestionId(),
                quest.getTitle(),
                quest.getMetric().name(),
                quest.getTargetVal(),
                quest.getBaselineVal(),
                quest.getBaselineMeasuredAt(),
                quest.getRewardXp(),
                quest.getExpiredAt()
        );
    }
}
