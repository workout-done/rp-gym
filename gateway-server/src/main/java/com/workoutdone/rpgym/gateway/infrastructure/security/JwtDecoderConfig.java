package com.workoutdone.rpgym.gateway.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import javax.crypto.SecretKey;

import com.workoutdone.rpgym.common.jwt.JwtSecretKeyFactory;

@Configuration
public class JwtDecoderConfig {

    @Bean
    public ReactiveJwtDecoder jwtDecoder(
            @Value("${jwt.secret}") String secret
    ) {

        // JWT Secret을 기반으로 서명 검증에 사용할 대칭키 생성
        SecretKey key = JwtSecretKeyFactory.create(secret);

        // HS256 알고리즘을 사용하는 JWT Decoder 생성
        return NimbusReactiveJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}