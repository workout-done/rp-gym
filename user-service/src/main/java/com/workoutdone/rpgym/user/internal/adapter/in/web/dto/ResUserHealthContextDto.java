package com.workoutdone.rpgym.user.internal.adapter.in.web.dto;

import com.workoutdone.rpgym.user.internal.application.GetUserHealthContextResult;
import com.workoutdone.rpgym.user.internal.application.LongTermGoalSummary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResUserHealthContextDto {

    private ResHealthProfileSummaryDto healthProfile;
    private ResDailyGoalSummaryDto dailyGoal;
    private List<ResLongTermGoalSummaryDto> longTermGoals;

    public static ResUserHealthContextDto from(GetUserHealthContextResult result) {
        return ResUserHealthContextDto.builder()
                .healthProfile(ResHealthProfileSummaryDto.from(result.getHealthProfile()))
                .dailyGoal(ResDailyGoalSummaryDto.from(result.getDailyGoal()))
                .longTermGoals(toLongTermGoalDtos(result.getLongTermGoals()))
                .build();
    }

    private static List<ResLongTermGoalSummaryDto> toLongTermGoalDtos(List<LongTermGoalSummary> longTermGoals) {
        if (longTermGoals == null) {
            return null;
        }

        return longTermGoals.stream()
                .map(ResLongTermGoalSummaryDto::from)
                .toList();
    }
}
