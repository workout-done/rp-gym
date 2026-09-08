package com.workoutdone.rpgym.user.internal.application;

import com.workoutdone.rpgym.user.dailyhealthgoal.domain.DailyHealthGoal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyGoalSummary {

    private Integer stepGoal;
    private Integer activeMinutesGoal;
    private Integer activeCaloriesGoal;

    public static DailyGoalSummary from(DailyHealthGoal dailyHealthGoal) {
        return DailyGoalSummary.builder()
                .stepGoal(dailyHealthGoal.getStepGoal())
                .activeMinutesGoal(dailyHealthGoal.getActiveMinutesGoal())
                .activeCaloriesGoal(dailyHealthGoal.getActiveCaloriesGoal())
                .build();
    }
}
