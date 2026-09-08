package com.workoutdone.rpgym.game.quest.application.payload;

import com.workoutdone.rpgym.game.quest.domain.aggregate.Quest;

import java.time.Instant;
import java.util.UUID;

public record QuestCompletedData(
        UUID questId,
        String metric,
        int targetValue,
        int baselineValue,
        int achievedDelta,
        int rewardXp,
        Instant completedByMeasuredAt
) {

    public static QuestCompletedData from(Quest quest, int achievedDelta, Instant completedByMeasuredAt) {
        return new QuestCompletedData(
                quest.getQuestId(),
                quest.getMetric().name(),
                quest.getTargetVal(),
                quest.getBaselineVal(),
                achievedDelta,
                quest.getRewardXp(),
                completedByMeasuredAt
        );
    }
}
