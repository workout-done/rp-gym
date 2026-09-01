package com.workoutdone.rpgym.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    INVALID_INPUT(
            "INVALID_INPUT",
            HttpStatus.BAD_REQUEST,
            "입력값이 올바르지 않습니다."
    ),

    INVALID_SORT_FIELD(
            "INVALID_SORT_FIELD",
            HttpStatus.BAD_REQUEST,
            "정렬 필드가 올바르지 않습니다."
    ),

    UNAUTHORIZED(
            "UNAUTHORIZED",
            HttpStatus.UNAUTHORIZED,
            "인증이 필요합니다."
    ),

    FORBIDDEN(
            "FORBIDDEN",
            HttpStatus.FORBIDDEN,
            "접근 권한이 없습니다."
    ),

    RESOURCE_NOT_FOUND(
            "RESOURCE_NOT_FOUND",
            HttpStatus.NOT_FOUND,
            "리소스를 찾을 수 없습니다."
    ),

    SERVICE_UNAVAILABLE(
            "SERVICE_UNAVAILABLE",
            HttpStatus.SERVICE_UNAVAILABLE,
            "하위 서비스를 사용할 수 없습니다."
    ),

    INTERNAL_ERROR(
            "INTERNAL_ERROR",
            HttpStatus.INTERNAL_SERVER_ERROR,
            "서버 오류가 발생했습니다."
    );

    private final String code;
    private final HttpStatus status;
    private final String message;
}