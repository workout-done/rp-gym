package com.workoutdone.rpgym.common.security;

import com.workoutdone.rpgym.common.constant.HeaderConstants;
import com.workoutdone.rpgym.common.exception.BaseException;
import com.workoutdone.rpgym.common.exception.CommonErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * API에 지정된 RequireRole을 기준으로 사용자의 Role을 검증한다.
 *
 * Gateway에서 JWT를 검증한 후 전달한 X-User-Role Header를 사용하며,
 * 각 서비스에서는 이 인터셉터를 통해 API별 접근 권한을 검증한다.
 */
public class RoleAuthorizationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {
        /*
         * Controller 메서드가 아닌 정적 리소스 등의 요청은
         * Role 검증 대상이 아니므로 그대로 통과시킨다.
         */
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        /*
         * 먼저 메서드에 선언된 RequireRole을 확인한다.
         *
         * 예:
         * @RequireRole(UserRole.USER)
         */
        RequireRole requireRole =
                handlerMethod.getMethodAnnotation(RequireRole.class);

        /*
         * 메서드에 RequireRole이 없다면
         * Controller 클래스에 선언된 RequireRole을 확인한다.
         *
         * 예:
         * @RequireRole(UserRole.ADMIN)
         * public class AdminController { ... }
         */
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType()
                    .getAnnotation(RequireRole.class);
        }

        /*
         * RequireRole이 지정되지 않은 API는
         * Role 제한이 없는 API이므로 그대로 통과시킨다.
         */
        if (requireRole == null) {
            return true;
        }

        /*
         * Gateway에서 전달한 검증된 사용자 Role을 조회한다.
         */
        String roleHeader = request.getHeader(HeaderConstants.USER_ROLE);

        /*
         * Gateway에서 전달한 사용자 Role이 없다면
         * 인증된 사용자 정보가 전달되지 않은 요청으로 판단하여
         * 인증 실패 처리한다.
         */
        if (roleHeader == null || roleHeader.isBlank()) {
            throw new BaseException(CommonErrorCode.UNAUTHORIZED);
        }

        /*
         * API에서 허용한 Role 중 현재 사용자의 Role이 포함되어 있는지 확인한다.
         */
        boolean allowed = Arrays.stream(requireRole.value())
                .anyMatch(role -> role.name().equals(roleHeader));

        /*
         * Role은 존재하지만 해당 API에서 허용하지 않는 Role이라면
         * 인증은 되었지만 권한이 없는 요청이므로 403을 반환한다.
         */
        if (!allowed) {
            throw new BaseException(CommonErrorCode.FORBIDDEN);
        }

        /*
         * 허용된 Role이라면 Controller 로직을 계속 실행한다.
         */
        return true;
    }
}