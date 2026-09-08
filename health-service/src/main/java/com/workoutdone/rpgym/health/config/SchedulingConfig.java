package com.workoutdone.rpgym.health.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 서비스 전역 스케줄링 활성화.
 * 현재 사용처는 Outbox 발행기(OutboxPublishScheduler)다.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}