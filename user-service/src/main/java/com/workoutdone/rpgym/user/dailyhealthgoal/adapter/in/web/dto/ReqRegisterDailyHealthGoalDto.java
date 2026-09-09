package com.workoutdone.rpgym.user.dailyhealthgoal.adapter.in.web.dto;

import com.workoutdone.rpgym.user.dailyhealthgoal.application.RegisterDailyHealthGoalCommand;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
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

    @Positive(message = "목표 걸음 수는 0보다 커야 합니다.")
    @Max(value = 50000, message = "목표 걸음 수는 50,000보를 초과할 수 없습니다.")
    private Integer stepGoal;

    @Positive(message = "목표 활동 시간은 0보다 커야 합니다.")
    @Max(value = 300, message = "목표 활동 시간은 300분을 초과할 수 없습니다.")
    private Integer activeMinutesGoal;

    @Positive(message = "목표 활동 칼로리는 0보다 커야 합니다.")
    @Max(value = 3000, message = "목표 활동 칼로리는 3,000kcal를 초과할 수 없습니다.")
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
