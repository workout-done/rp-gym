package com.workoutdone.rpgym.game.character.exception;

import com.workoutdone.rpgym.common.exception.CommonErrorCode;
import com.workoutdone.rpgym.common.response.ErrorResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * game-service 전용 예외 처리.
 *
 * <p>응답 스키마와 에러 코드는 common 모듈({@link ErrorResponse}, {@link CommonErrorCode}) 을 그대로 쓴다.
 * 인증은 API Gateway 가 담당하므로 여기서는 인증 관련 예외를 다루지 않는다.
 * common 의 {@link com.workoutdone.rpgym.common.exception.GlobalExceptionHandler} 가 가진
 * catch-all {@code Exception} 핸들러보다 먼저 잡아야 하므로 우선순위를 최상위로 둔다.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GameExceptionHandler {

    // PathVariable UUID 파싱 실패.
    // /characters/{userId} 로 들어오는 값은 게이트웨이가 검증하지 않으므로 여기서 막는다.
    // X-User-Id 헤더는 게이트웨이가 JWT 에서 만들어 넣어주므로 파싱 실패를 가정하지 않는다.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return build(CommonErrorCode.INVALID_INPUT, e.getName() + " 값이 올바르지 않습니다.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return build(CommonErrorCode.INVALID_INPUT, e.getMessage());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException e) {
    // 게이트웨이를 거쳤다면 반드시 있어야 하는 헤더다. 없다는 건 인증 경로를 우회했다는 뜻.
    return build(CommonErrorCode.UNAUTHORIZED, "인증 정보가 없습니다.");
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
