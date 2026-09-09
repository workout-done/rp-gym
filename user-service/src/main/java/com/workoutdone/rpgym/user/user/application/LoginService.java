package com.workoutdone.rpgym.user.user.application;

import com.workoutdone.rpgym.common.exception.BaseException;
import com.workoutdone.rpgym.user.user.adapter.out.jwt.JwtProvider;
import com.workoutdone.rpgym.user.user.adapter.out.redis.RefreshTokenStore;
import com.workoutdone.rpgym.user.user.domain.User;
import com.workoutdone.rpgym.user.user.domain.UserErrorCode;
import com.workoutdone.rpgym.user.user.domain.UserRepository;
import com.workoutdone.rpgym.user.user.domain.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenStore refreshTokenStore;

    private static final String TOKEN_TYPE = "Bearer";

    public LoginResult login(LoginCommand command) {
        String normalizedEmail = User.normalizeEmail(command.getEmail());

        // 이메일 존재 여부와 비밀번호 일치 여부는 구분해서 안내하지 않음(LOGIN_FAILED로 통일)
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BaseException(UserErrorCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(command.getRawPassword(), user.getPassword())) {
            throw new BaseException(UserErrorCode.LOGIN_FAILED);
        }

        // 비밀번호가 일치하더라도 탈퇴/정지 계정은 로그인을 차단
        // 탈퇴 여부는 별도로 노출하지 않고 LOGIN_FAILED로 통일해서 응답
        if (user.getDeletedAt() != null) {
            throw new BaseException(UserErrorCode.LOGIN_FAILED);
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new BaseException(UserErrorCode.ACCOUNT_SUSPENDED);
        }

        //로그인 성공 시 Access Token, Refresh Token 발급
        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getRole());
        String refreshToken = UUID.randomUUID().toString();

        refreshTokenStore.save(refreshToken, user.getId()); //Redis에 Refresh Token 저장

        return LoginResult.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(TOKEN_TYPE)
                .expiresIn(jwtProvider.getAccessTokenExpirySeconds())
                .build();
    }
}
