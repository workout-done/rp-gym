package com.workoutdone.rpgym.user.healthprofile.adapter.in.web.dto;

import com.workoutdone.rpgym.user.healthprofile.application.RegisterHealthProfileCommand;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReqRegisterHealthProfileDto {

    @NotNull(message = "키를 입력해주세요.")
    @DecimalMin(value = "0.0", inclusive = false, message = "키는 0보다 커야 합니다.")
    @Digits(integer = 3, fraction = 2, message = "키는 소수점 둘째 자리까지 입력 가능합니다.")
    private BigDecimal height;

    @NotNull(message = "몸무게를 입력해주세요.")
    @DecimalMin(value = "0.0", inclusive = false, message = "몸무게는 0보다 커야 합니다.")
    @Digits(integer = 3, fraction = 2, message = "몸무게는 소수점 둘째 자리까지 입력 가능합니다.")
    private BigDecimal weight;

    public RegisterHealthProfileCommand toCommand(UUID userId) {
        return RegisterHealthProfileCommand.builder()
                .userId(userId)
                .height(height)
                .weight(weight)
                .build();
    }
}
