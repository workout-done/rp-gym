package com.workoutdone.rpgym.gateway.infrastructure.security;

import com.workoutdone.rpgym.common.jwt.JwtClaimConstants;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class RequiredClaimsValidator implements OAuth2TokenValidator<Jwt> {

    // 필수 Claim이 없는 JWT를 유효하지 않은 토큰으로 처리하기 위한 오류
    private static final OAuth2Error INVALID_TOKEN =
            new OAuth2Error("invalid_token");

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {

        // 사용자 식별에 필요한 sub Claim이 존재하는지 검증
        if (jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            return OAuth2TokenValidatorResult.failure(INVALID_TOKEN);
        }

        // 권한 정보에 필요한 role Claim이 존재하는지 검증
        String role = jwt.getClaimAsString(JwtClaimConstants.ROLE);

        if (role == null || role.isBlank()) {
            return OAuth2TokenValidatorResult.failure(INVALID_TOKEN);
        }

        // 필수 Claim이 모두 존재하면 검증 성공
        return OAuth2TokenValidatorResult.success();
    }
}