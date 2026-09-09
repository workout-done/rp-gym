package com.workoutdone.rpgym.health.summary.adapter.out;

public record UserHealthContextResponse(
        DailyGoal dailyGoal
) {
    public record DailyGoal(
            Integer stepGoal,
            Integer activeMinutesGoal,
            Integer activeCaloriesGoal
    ) {
    }
}