package com.workoutdone.rpgym.health.exception;

import com.workoutdone.rpgym.common.exception.CommonErrorCode;
import com.workoutdone.rpgym.common.response.ErrorResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * health-service 전용 예외 처리.
 *
 * <p>응답 스키마와 에러 코드는 common 모듈({@link ErrorResponse}, {@link CommonErrorCode})을 그대로 쓴다.
 * common의 {@link com.workoutdone.rpgym.common.exception.GlobalExceptionHandler}가 가진
 * catch-all {@code Exception} 핸들러보다 먼저 잡아야 하므로 우선순위를 최상위로 둔다.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class HealthExceptionHandler {

    /**
     * X-User-Id 헤더 누락 = 게이트웨이를 거치지 않은 요청.
     *
     * <p>API 공통 규격 3-1: "각 서비스는 X-User-Id, X-User-Role 헤더가 없으면 401으로 응답한다."
     * 이 핸들러가 없으면 common의 catch-all이 잡아 500 INTERNAL_ERROR로 응답하고,
     * 클라이언트 오류가 error 로그로 쌓여 진짜 버그가 묻힌다. 게이트웨이 도입 후에도 유지한다.
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException e) {
        if ("X-User-Id".equalsIgnoreCase(e.getHeaderName())) {
            return build(CommonErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return build(CommonErrorCode.INVALID_INPUT, e.getHeaderName() + " 헤더가 필요합니다.");
    }

    /** UUID 파싱 실패. 헤더에서 온 값이면 인증 정보 문제이므로 401. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        if (e.getParameter().hasParameterAnnotation(RequestHeader.class)) {
            return build(CommonErrorCode.UNAUTHORIZED, "유효하지 않은 인증 정보입니다.");
        }
        return build(CommonErrorCode.INVALID_INPUT, e.getName() + " 값이 올바르지 않습니다.");
    }

    private ResponseEntity<ErrorResponse> build(CommonErrorCode errorCode, String message) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode.getCode(), message, traceId()));
    }

    private String traceId() {
        return MDC.get("traceId");
    }
}