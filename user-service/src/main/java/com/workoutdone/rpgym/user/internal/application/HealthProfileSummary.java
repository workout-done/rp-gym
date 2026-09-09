package com.workoutdone.rpgym.user.internal.application;

import com.workoutdone.rpgym.user.healthprofile.domain.HealthProfile;
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

    public static HealthProfileSummary from(HealthProfile healthProfile) {
        return HealthProfileSummary.builder()
                .height(healthProfile.getHeight())
                .weight(healthProfile.getWeight())
                .build();
    }
}
