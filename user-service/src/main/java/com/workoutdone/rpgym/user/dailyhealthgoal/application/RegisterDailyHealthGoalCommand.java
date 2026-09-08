package com.workoutdone.rpgym.user.dailyhealthgoal.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterDailyHealthGoalCommand {

    private UUID userId;
    private Integer stepGoal;
    private Integer activeMinutesGoal;
    private Integer activeCaloriesGoal;
}
