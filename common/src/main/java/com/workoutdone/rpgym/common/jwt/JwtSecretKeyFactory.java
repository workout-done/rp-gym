package com.workoutdone.rpgym.common.jwt;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public final class JwtSecretKeyFactory {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private JwtSecretKeyFactory() {
    }

    public static SecretKey create(String secret) {
        return new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                HMAC_SHA256
        );
    }
}