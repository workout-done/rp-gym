package com.workoutdone.rpgym.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL) // 값이 null인 필드는 JSON으로 변환할 때 제외
public record ErrorResponse(
        String code,
        String message,
        String traceId,
        List<FieldError> fields
) {

    public record FieldError(
            String field,
            String reason
    ) {
    }

    public static ErrorResponse of(
            String code,
            String message,
            String traceId
    ) {
        return new ErrorResponse(
                code,
                message,
                traceId,
                null
        );
    }

    public static ErrorResponse of(
            String code,
            String message,
            String traceId,
            List<FieldError> fields
    ) {
        return new ErrorResponse(
                code,
                message,
                traceId,
                fields
        );
    }
}