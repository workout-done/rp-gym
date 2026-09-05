package com.workoutdone.rpgym.gateway.infrastructure.security;

import com.workoutdone.rpgym.common.jwt.JwtClaimConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

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
                // 검증된 JWT의 role Claim을 Spring Security 권한으로 변환
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
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

    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        // JWT의 role Claim을 Spring Security의 GrantedAuthority로 변환
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String role = jwt.getClaimAsString(JwtClaimConstants.ROLE);

            // role이 없거나 비어 있으면 권한을 부여하지 않음
            if (role == null || role.isBlank()) {
                return List.of();
            }

            // hasRole()과 호환되도록 ROLE_ 접두사를 붙여 권한 생성
            return List.of(new SimpleGrantedAuthority("ROLE_" + role));
        });

        // WebFlux 환경에서 사용할 수 있도록 Reactive Converter로 변환
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }

}