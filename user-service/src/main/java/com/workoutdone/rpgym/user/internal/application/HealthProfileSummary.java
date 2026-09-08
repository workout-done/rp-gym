package com.workoutdone.rpgym.user.internal.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthProfileSummary {

    private BigDecimal height;
    private BigDecimal weight;
}
