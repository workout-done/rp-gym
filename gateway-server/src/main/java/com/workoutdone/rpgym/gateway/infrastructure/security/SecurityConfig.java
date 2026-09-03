package com.workoutdone.rpgym.gateway.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http
    ) {
        return http
                // CSRF 비활성화
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // 보안 정책 설정
                .authorizeExchange(exchange -> exchange
                        // 인증 없이 접근 가능한 API
                        .pathMatchers(
                                "/api/v1/users/login",
                                "/api/v1/users/signup",
                                "/api/v1/users/refresh"
                        ).permitAll()

                        // 그 외 모든 API는 인증 필요
                        // Spring Security가 인증 정보를 기반으로 인가 처리
                        .anyExchange().authenticated()
                )

                // JWT 기반 인증 설정
                // ReactiveJwtDecoder를 사용해서 Bearer Access Token을 JWT로 검증
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {})
                )

                // 인증/인가 예외 처리
                .exceptionHandling(exception -> exception
                        // 인증 실패 시 401 Unauthorized
                        .authenticationEntryPoint(
                                (exchange, ex) -> {
                                    exchange.getResponse()
                                            .setStatusCode(HttpStatus.UNAUTHORIZED);

                                    return exchange.getResponse().setComplete();
                                }
                        )
                        // 인가 실패 시 403 Forbidden
                        .accessDeniedHandler(
                                (exchange, ex) -> {
                                    exchange.getResponse()
                                            .setStatusCode(HttpStatus.FORBIDDEN);

                                    return exchange.getResponse().setComplete();
                                }
                        )
                )
                .build();
    }
}