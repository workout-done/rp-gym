package com.workoutdone.rpgym.user.dailyhealthgoal.adapter.in.web.dto;

import com.workoutdone.rpgym.user.dailyhealthgoal.application.RegisterDailyHealthGoalCommand;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReqRegisterDailyHealthGoalDto {

    @PositiveOrZero(message = "목표 걸음 수는 0 이상이어야 합니다.")
    private Integer stepGoal;

    @PositiveOrZero(message = "목표 활동 시간은 0 이상이어야 합니다.")
    private Integer activeMinutesGoal;

    @PositiveOrZero(message = "목표 활동 칼로리는 0 이상이어야 합니다.")
    private Integer activeCaloriesGoal;

    public RegisterDailyHealthGoalCommand toCommand(UUID userId) {
        return RegisterDailyHealthGoalCommand.builder()
                .userId(userId)
                .stepGoal(stepGoal)
                .activeMinutesGoal(activeMinutesGoal)
                .activeCaloriesGoal(activeCaloriesGoal)
                .build();
    }
}
