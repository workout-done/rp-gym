package com.workoutdone.rpgym.user.user.adapter.out.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
//Refresh Token 저장
public class RefreshTokenStore {

    private static final String KEY_PREFIX = "refresh-token:";
    private static final Duration TTL = Duration.ofDays(7); //Refresh Token 만료시간(7일)

    private final StringRedisTemplate redisTemplate;

    // 재발급/로그아웃 시 유효성 검증을 위해 Redis에 저장 (key: refreshToken, value: userId)
    public void save(String refreshToken, UUID userId) {
        redisTemplate.opsForValue().set(KEY_PREFIX + refreshToken, userId.toString(), TTL);
    }
}
