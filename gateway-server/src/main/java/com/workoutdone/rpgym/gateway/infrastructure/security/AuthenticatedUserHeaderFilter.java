package com.workoutdone.rpgym.gateway.infrastructure.security;

import com.workoutdone.rpgym.common.constant.HeaderConstants;
import com.workoutdone.rpgym.common.jwt.JwtClaimConstants;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticatedUserHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain
    ) {
        // 클라이언트가 전달한 사용자 정보 Header를 먼저 제거
        // 인증되지 않은 요청에서도 위조된 사용자 정보가 하위 서비스로 전달되지 않도록 함
        ServerWebExchange sanitizedExchange = exchange.mutate()
                .request(request -> request.headers(headers -> {
                    headers.remove(HeaderConstants.USER_ID);
                    headers.remove(HeaderConstants.USER_ROLE);
                }))
                .build();

        return exchange.getPrincipal()
                .cast(Authentication.class)
                .map(authentication -> {
                    Jwt jwt = (Jwt) authentication.getPrincipal();

                    String userId = jwt.getSubject();
                    String role = jwt.getClaimAsString(JwtClaimConstants.ROLE);

                    // 검증된 JWT Claim을 사용자 정보 Header로 전달
                    return sanitizedExchange.mutate()
                            .request(request -> request.headers(headers -> {
                                headers.add(HeaderConstants.USER_ID, userId);
                                headers.add(HeaderConstants.USER_ROLE, role);
                            }))
                            .build();
                })
                // 인증 정보가 없으면 Header를 제거한 요청을 그대로 전달
                .defaultIfEmpty(sanitizedExchange)
                // 인증 여부에 따라 결정된 요청을 하위 필터 체인으로 한 번만 전달
                .flatMap(chain::filter);
    }

    @Override
    public int getOrder() {
        // 실제 하위 서비스 요청이 전송되기 전에 사용자 정보 Header를 처리
        return 0;
    }
}