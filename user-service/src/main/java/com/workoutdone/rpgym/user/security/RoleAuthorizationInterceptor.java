package com.workoutdone.rpgym.user.security;

import com.workoutdone.rpgym.common.constant.HeaderConstants;
import com.workoutdone.rpgym.common.exception.BaseException;
import com.workoutdone.rpgym.common.exception.CommonErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

public class RoleAuthorizationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole == null) {
            return true;
        }

        String roleHeader = request.getHeader(HeaderConstants.USER_ROLE);

        // 헤더가 아예 없으면 게이트웨이를 거치지 않은 인증 안 된 요청으로 취급한다.
        if (roleHeader == null || roleHeader.isBlank()) {
            throw new BaseException(CommonErrorCode.UNAUTHORIZED);
        }

        boolean allowed = Arrays.stream(requireRole.value())
                .anyMatch(role -> role.name().equals(roleHeader));

        // 인증은 됐지만 이 API가 허용하는 role이 아니면 403으로 차단한다.
        if (!allowed) {
            throw new BaseException(CommonErrorCode.FORBIDDEN);
        }

        return true;
    }
}
