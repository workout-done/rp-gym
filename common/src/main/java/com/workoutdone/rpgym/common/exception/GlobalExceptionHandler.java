package com.workoutdone.rpgym.common.exception;

import com.workoutdone.rpgym.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 공통 비즈니스 예외 처리
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(
            BaseException exception
    ) {
        ErrorCode errorCode = exception.getErrorCode();

        logException(errorCode, exception);

        ErrorResponse response = ErrorResponse.of(
                errorCode.getCode(),
                errorCode.getMessage(),
                getTraceId()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    /**
     * Spring Validator 검증 실패 처리
     *
     * 예:
     * @NotBlank
     * @Size
     * @Min
     * 등으로 발생한 필드 오류를
     * fields 배열로 변환한다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception
    ) {

        List<ErrorResponse.FieldError> fields =
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error ->
                                new ErrorResponse.FieldError(
                                        error.getField(),
                                        error.getDefaultMessage()
                                )
                        )
                        .toList();

        ErrorResponse response = ErrorResponse.of(
                CommonErrorCode.INVALID_INPUT.getCode(),
                CommonErrorCode.INVALID_INPUT.getMessage(),
                getTraceId(),
                fields
        );

        log.warn(
                "Validation failed: fields={}",
                fields
        );

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    /**
     * 예상하지 못한 서버 오류 처리
     *
     * 클라이언트에는 상세 예외 정보를 노출하지 않는다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception exception
    ) {

        log.error("Unhandled exception", exception);

        CommonErrorCode errorCode =
                CommonErrorCode.INTERNAL_ERROR;

        ErrorResponse response = ErrorResponse.of(
                errorCode.getCode(),
                errorCode.getMessage(),
                getTraceId()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    /**
     * 현재 요청의 traceId를 MDC에서 조회한다.
     *
     * 추후 Micrometer Tracing / Zipkin 연동 시
     * tracing 설정에 의해 MDC에 들어온 traceId를 사용한다.
     */
    private String getTraceId() {
        return MDC.get("traceId");
    }

    /**
     * HTTP 상태에 따라 로그 레벨을 구분한다.
     */
    private void logException(
            ErrorCode errorCode,
            BaseException exception
    ) {

        if (errorCode.getStatus().is4xxClientError()) {
            log.warn(
                    "Business exception: code={}, message={}",
                    errorCode.getCode(),
                    errorCode.getMessage()
            );
        } else {
            log.error(
                    "Business exception: code={}, message={}",
                    errorCode.getCode(),
                    errorCode.getMessage(),
                    exception
            );
        }
    }
}