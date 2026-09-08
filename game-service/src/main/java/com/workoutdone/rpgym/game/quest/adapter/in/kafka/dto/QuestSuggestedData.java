package com.workoutdone.rpgym.game.quest.adapter.in.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * QUEST_SUGGESTED의 data.
 *
 * rewardXp가 없다. 보상 금액은 Game이 정한다 -- Health가 실어 보내기 시작하면
 * 무시할 것이 아니라 계약 위반으로 다뤄야 한다.
 *
 * metric을 String으로 받는다. AI가 만드는 값이라 3종 밖일 수 있고,
 * 파싱 실패가 곧 UNKNOWN_METRIC이라는 판정이다. Metric으로 받으면 그 판정을 할 수 없다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record QuestSuggestedData(
        UUID suggestionId,
        LocalDate activityDate,
        OffsetDateTime basedOnMeasuredAt,
        String title,
        String metric,
        int targetValue
) {
}
