package com.workoutdone.rpgym.gateway.infrastructure.security;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import com.workoutdone.rpgym.common.constant.HeaderConstants;
import com.workoutdone.rpgym.common.jwt.JwtClaimConstants;

@Component
public class AuthenticatedUserHeaderFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain
    ) {
        return exchange.getPrincipal()
                .cast(Authentication.class)
                .flatMap(authentication -> {

                    Jwt jwt = (Jwt) authentication.getPrincipal();

                    String userId = jwt.getSubject();
                    String role = jwt.getClaimAsString(JwtClaimConstants.ROLE);

                    // 인증된 사용자 정보를 Header로 전달
                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(request -> request.headers(headers -> {
                                // 기존 사용자 정보 Header 제거
                                headers.remove(HeaderConstants.USER_ID);
                                headers.remove(HeaderConstants.USER_ROLE);

                                // 검증된 JWT Claim으로 사용자 정보 Header 추가
                                headers.add(HeaderConstants.USER_ID, userId);
                                headers.add(HeaderConstants.USER_ROLE, role);
                            }))
                            .build();

                    return chain.filter(mutatedExchange);
                })
                // 인증 정보가 없는 요청은 그대로 전달
                .switchIfEmpty(chain.filter(exchange));
    }
}