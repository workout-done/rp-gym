package com.workoutdone.rpgym.user.security;

import com.workoutdone.rpgym.user.user.domain.UserRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Gateway가 전달한 X-User-Role 헤더가 여기 명시한 role 중 하나가 아니면 403으로 차단
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    UserRole[] value();
}
