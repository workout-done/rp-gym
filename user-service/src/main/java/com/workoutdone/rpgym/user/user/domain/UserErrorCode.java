package com.workoutdone.rpgym.user.user.domain;

import com.workoutdone.rpgym.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    EMAIL_DUPLICATED(
            "EMAIL_DUPLICATED",
            HttpStatus.CONFLICT,
            "이미 사용 중인 이메일입니다."
    ),

    NICKNAME_DUPLICATED(
            "NICKNAME_DUPLICATED",
            HttpStatus.CONFLICT,
            "이미 사용 중인 닉네임입니다."
    );

    private final String code;
    private final HttpStatus status;
    private final String message;
}
