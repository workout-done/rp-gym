package com.workoutdone.rpgym.health.config;

import com.workoutdone.rpgym.common.exception.GlobalExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * common 모듈의 공통 예외 처리를 health-service에 등록한다.
 *
 * <p>{@code @SpringBootApplication}의 컴포넌트 스캔 범위가 {@code com.workoutdone.rpgym.health}라
 * common 패키지의 {@link GlobalExceptionHandler}는 스캔되지 않는다. 명시적으로 import 해서
 * BaseException / 미처리 Exception 처리를 공통 규격으로 맞춘다.
 */
@Configuration
@Import(GlobalExceptionHandler.class)
public class CommonExceptionHandlerConfig {
}