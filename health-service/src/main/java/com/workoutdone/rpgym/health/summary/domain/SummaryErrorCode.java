package com.workoutdone.rpgym.health.summary.domain;

import com.workoutdone.rpgym.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SummaryErrorCode implements ErrorCode {

    INVALID_GOAL_METRIC_VALUE(
            "INVALID_GOAL_METRIC_VALUE",
            HttpStatus.BAD_REQUEST,
            "목표값과 달성값은 0 이상이어야 합니다."
    );

    private final String code;
    private final HttpStatus status;
    private final String message;
}