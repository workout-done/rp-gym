package com.workoutdone.rpgym.user.healthprofile.application;

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
public class RegisterHealthProfileCommand {

    private UUID userId;
    private BigDecimal height;
    private BigDecimal weight;
}
