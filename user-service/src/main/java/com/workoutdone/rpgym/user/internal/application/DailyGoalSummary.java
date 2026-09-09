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

    // 일일 목표를 미등록한 사용자에게 내려줄 기본값(5,000보/60분/300kcal)
    public static DailyGoalSummary defaultValue() {
        return DailyGoalSummary.builder()
                .stepGoal(DailyHealthGoal.DEFAULT_STEP_GOAL)
                .activeMinutesGoal(DailyHealthGoal.DEFAULT_ACTIVE_MINUTES_GOAL)
                .activeCaloriesGoal(DailyHealthGoal.DEFAULT_ACTIVE_CALORIES_GOAL)
                .build();
    }
}
