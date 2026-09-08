package com.workoutdone.rpgym.health.synthetic.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * enabled=true일 때만 Synthetic 관련 빈을 등록한다.
 * 설정이 없으면 프로퍼티 빈 자체가 만들어지지 않으므로 수집기도 함께 비활성화된다.
 */
@Configuration
@EnableConfigurationProperties(SyntheticProperties.class)
@ConditionalOnProperty(prefix = "rpgym.synthetic", name = "enabled", havingValue = "true")
public class SyntheticConfig {
}