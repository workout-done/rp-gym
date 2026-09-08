package com.workoutdone.rpgym.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 해당 API에 접근 가능한 Role을 지정한다.
 * 실제 Role 검증은 RoleAuthorizationInterceptor에서 처리한다.
 *
 * 예:
 * @RequireRole(UserRole.USER)
 * @RequireRole({UserRole.USER, UserRole.ADMIN})
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /**
     * 해당 API에 접근할 수 있는 Role 목록
     */
    UserRole[] value();
}