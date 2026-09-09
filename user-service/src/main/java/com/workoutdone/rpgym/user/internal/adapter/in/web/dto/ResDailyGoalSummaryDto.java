package com.workoutdone.rpgym.user.internal.adapter.in.web.dto;

import com.workoutdone.rpgym.user.internal.application.DailyGoalSummary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResDailyGoalSummaryDto {

    private Integer stepGoal;
    private Integer activeMinutesGoal;
    private Integer activeCaloriesGoal;

    public static ResDailyGoalSummaryDto from(DailyGoalSummary summary) {
        if (summary == null) {
            return null;
        }

        return ResDailyGoalSummaryDto.builder()
                .stepGoal(summary.getStepGoal())
                .activeMinutesGoal(summary.getActiveMinutesGoal())
                .activeCaloriesGoal(summary.getActiveCaloriesGoal())
                .build();
    }
}
