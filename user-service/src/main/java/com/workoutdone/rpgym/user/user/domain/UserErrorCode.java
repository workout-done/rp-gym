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
    ),

    // 이메일/비밀번호 불일치, 탈퇴한 계정, 존재하지 않는 계정을 모두 이 코드 하나로 응답
    LOGIN_FAILED(
            "LOGIN_FAILED",
            HttpStatus.UNAUTHORIZED,
            "이메일 또는 비밀번호가 일치하지 않습니다."
    ),

    ACCOUNT_SUSPENDED(
            "ACCOUNT_SUSPENDED",
            HttpStatus.CONFLICT,
            "정지된 계정입니다."
    );

    private final String code;
    private final HttpStatus status;
    private final String message;
}
