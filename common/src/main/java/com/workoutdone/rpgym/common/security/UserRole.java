package com.workoutdone.rpgym.common.security;

/**
 * Gateway에서는 JWT의 role 값이 유효한지 검증하고,
 * 각 하위 서비스에서는 API별 접근 가능한 Role을 검증할 때 사용한다.
 */
public enum UserRole {
    USER,
    ADMIN
}