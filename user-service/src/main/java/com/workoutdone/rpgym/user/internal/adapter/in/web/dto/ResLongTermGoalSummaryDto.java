package com.workoutdone.rpgym.user.internal.adapter.in.web.dto;

import com.workoutdone.rpgym.user.internal.application.LongTermGoalSummary;
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
public class ResLongTermGoalSummaryDto {

    private UUID id;
    private String goalType;
    private BigDecimal targetValue;
    private String targetUnit;
    private String status;

    public static ResLongTermGoalSummaryDto from(LongTermGoalSummary summary) {
        return ResLongTermGoalSummaryDto.builder()
                .id(summary.getId())
                .goalType(summary.getGoalType())
                .targetValue(summary.getTargetValue())
                .targetUnit(summary.getTargetUnit())
                .status(summary.getStatus())
                .build();
    }
}
