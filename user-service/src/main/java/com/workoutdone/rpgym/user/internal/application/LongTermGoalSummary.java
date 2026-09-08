package com.workoutdone.rpgym.user.internal.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

////TO-DO: 장기 목표 기능이 구현되면 이 타입으로 IN_PROGRESS 상태인 목표 목록을 채운다.
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LongTermGoalSummary {

    private UUID id;
    private String goalType;
    private BigDecimal targetValue;
    private String targetUnit;
    private String status;
}
