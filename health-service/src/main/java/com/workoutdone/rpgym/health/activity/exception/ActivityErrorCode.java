package com.workoutdone.rpgym.health.activity.exception;

import com.workoutdone.rpgym.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ActivityErrorCode implements ErrorCode {

    INVALID_ACTIVITY_VALUE("INVALID_ACTIVITY_VALUE", HttpStatus.BAD_REQUEST, "건강 활동 값은 0 이상이어야 합니다."),
    HEALTH_ACTIVITY_NOT_FOUND("HEALTH_ACTIVITY_NOT_FOUND", HttpStatus.NOT_FOUND, "건강 활동 데이터를 찾을 수 없습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
