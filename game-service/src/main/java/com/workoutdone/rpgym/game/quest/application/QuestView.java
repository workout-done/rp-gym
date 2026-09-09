package com.workoutdone.rpgym.game.quest.application;

import com.workoutdone.rpgym.game.quest.domain.Metric;
import com.workoutdone.rpgym.game.quest.domain.QuestStatus;
import com.workoutdone.rpgym.game.quest.domain.aggregate.Quest;

import java.time.Instant;
import java.util.UUID;

/**
 * 조회 경계 타입.
 * 도메인 Quest를 그대로 웹으로 넘기지 않는다.
 * 도메인 엔티티도 어댑터로 나가지 않아야한다. 나가면 컬럼 하나 바꿀 때마다 응답 스펙에 따라 바뀐다.
 * currentValue는 quest.progress()가 계산한다 -> 달성분 = last_cumulative_val - baseline_val.
 * 첫 이벤트 전이면 0이다.
 */
public record QuestView(
        UUID questId,
        String title,
        Metric metric,
        int targetValue,
        int baselineValue,
        int currentValue,
        QuestStatus status,
        int rewardXp,
        Instant expiredAt
) {

    public static QuestView from(Quest quest) {
        return new QuestView(
                quest.getQuestId(),
                quest.getTitle(),
                quest.getMetric(),
                quest.getTargetVal(),
                quest.getBaselineVal(),
                quest.progress(),
                quest.getStatus(),
                quest.getRewardXp(),
                quest.getExpiredAt()
        );
    }
}
