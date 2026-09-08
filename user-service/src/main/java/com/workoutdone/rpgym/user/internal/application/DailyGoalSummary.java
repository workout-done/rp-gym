package com.workoutdone.rpgym.user.internal.application;

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
}
