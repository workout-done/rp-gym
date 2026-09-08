package com.workoutdone.rpgym.user.internal.adapter.in.web.dto;

import com.workoutdone.rpgym.user.internal.application.HealthProfileSummary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResHealthProfileSummaryDto {

    private BigDecimal height;
    private BigDecimal weight;

    public static ResHealthProfileSummaryDto from(HealthProfileSummary summary) {
        if (summary == null) {
            return null;
        }

        return ResHealthProfileSummaryDto.builder()
                .height(summary.getHeight())
                .weight(summary.getWeight())
                .build();
    }
}
