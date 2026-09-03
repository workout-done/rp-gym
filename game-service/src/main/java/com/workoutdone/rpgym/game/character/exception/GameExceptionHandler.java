package com.workoutdone.rpgym.game.character.exception;

import com.workoutdone.rpgym.common.exception.CommonErrorCode;
import com.workoutdone.rpgym.common.response.ErrorResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/**
 * game-service 전용 예외 처리.
 *
 * <p>응답 스키마와 에러 코드는 common 모듈({@link ErrorResponse}, {@link CommonErrorCode}) 을 그대로 쓴다.
 * common 의 {@code GlobalExceptionHandler} 보다 먼저 잡아야 하므로 우선순위를 최상위로 둔다.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GameExceptionHandler {

    // X-User-Id 헤더 누락 = 인증정보 없음
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException e){
        if ("X-User-Id".equalsIgnoreCase(e.getHeaderName())){
            return build(CommonErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        return build(CommonErrorCode.INVALID_INPUT, e.getHeaderName() + " 헤더가 필요합니다.");
    }

    // UUID 파싱 실패
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        boolean fromHeader = e.getParameter().hasParameterAnnotation(RequestHeader.class);

        if (fromHeader) {
            return build(CommonErrorCode.UNAUTHORIZED, "유효하지 않은 인증 정보입니다.");
        }
        return build(CommonErrorCode.INVALID_INPUT, e.getName() + " 값이 올바르지 않습니다.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        List<ErrorResponse.FieldError> fields = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();

        ErrorResponse response = ErrorResponse.of(
                CommonErrorCode.INVALID_INPUT.getCode(),
                CommonErrorCode.INVALID_INPUT.getMessage(),
                traceId(),
                fields
        );
        return ResponseEntity.status(CommonErrorCode.INVALID_INPUT.getStatus()).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return build(CommonErrorCode.INVALID_INPUT, e.getMessage());
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
