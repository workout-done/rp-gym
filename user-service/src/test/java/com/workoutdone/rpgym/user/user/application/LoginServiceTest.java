package com.workoutdone.rpgym.user.user.application;

import com.workoutdone.rpgym.common.exception.BaseException;
import com.workoutdone.rpgym.user.user.adapter.out.jwt.JwtProvider;
import com.workoutdone.rpgym.user.user.adapter.out.redis.RefreshTokenStore;
import com.workoutdone.rpgym.user.user.domain.User;
import com.workoutdone.rpgym.user.user.domain.UserErrorCode;
import com.workoutdone.rpgym.user.user.domain.UserRepository;
import com.workoutdone.rpgym.user.user.domain.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RefreshTokenStore refreshTokenStore;

    @InjectMocks
    private LoginService loginService;

    private User activeUser() {
        User user = User.create("healthuser@example.com", "encoded-password", "헬스퀘스트유저", "U0123ABC456");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }

    private LoginCommand command() {
        return LoginCommand.builder()
                .email("healthuser@example.com")
                .rawPassword("myPassw0rd!")
                .build();
    }

    @Test
    @DisplayName("이메일/비밀번호가 일치하는 활성 계정이면 토큰을 발급하고 Refresh Token을 Redis에 저장한다")
    void login_success() {
        User user = activeUser();
        LoginCommand command = command();
        given(userRepository.findByEmail("healthuser@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches(command.getRawPassword(), user.getPassword())).willReturn(true);
        given(jwtProvider.createAccessToken(user.getId(), user.getRole())).willReturn("access-token");
        given(jwtProvider.getAccessTokenExpirySeconds()).willReturn(1800L);

        LoginResult result = loginService.login(command);

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getTokenType()).isEqualTo("Bearer");
        assertThat(result.getExpiresIn()).isEqualTo(1800L);
        assertThat(result.getRefreshToken()).isNotBlank();
        verify(refreshTokenStore).save(result.getRefreshToken(), user.getId());
    }

    @Test
    @DisplayName("이메일 대소문자가 달라도 정규화된 값으로 사용자를 조회한다")
    void login_normalizesEmailCase() {
        User user = activeUser();
        LoginCommand command = LoginCommand.builder()
                .email("HealthUser@Example.com")
                .rawPassword("myPassw0rd!")
                .build();
        given(userRepository.findByEmail("healthuser@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(jwtProvider.createAccessToken(any(), any())).willReturn("access-token");
        given(jwtProvider.getAccessTokenExpirySeconds()).willReturn(1800L);

        loginService.login(command);

        verify(userRepository).findByEmail("healthuser@example.com");
    }

    @Test
    @DisplayName("존재하지 않는 이메일이면 LOGIN_FAILED 예외를 던지고 토큰을 발급하지 않는다")
    void login_emailNotFound() {
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        assertThatThrownBy(() -> loginService.login(command()))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> assertThat(((BaseException) ex).getErrorCode()).isEqualTo(UserErrorCode.LOGIN_FAILED));

        verify(refreshTokenStore, never()).save(anyString(), any());
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 LOGIN_FAILED 예외를 던지고 토큰을 발급하지 않는다")
    void login_passwordMismatch() {
        User user = activeUser();
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        assertThatThrownBy(() -> loginService.login(command()))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> assertThat(((BaseException) ex).getErrorCode()).isEqualTo(UserErrorCode.LOGIN_FAILED));

        verify(refreshTokenStore, never()).save(anyString(), any());
    }

    @Test
    @DisplayName("탈퇴한 계정이면 비밀번호가 일치해도 LOGIN_FAILED 예외를 던진다(탈퇴 여부는 노출하지 않음)")
    void login_withdrawnAccount() {
        User user = activeUser();
        ReflectionTestUtils.setField(user, "deletedAt", LocalDateTime.now());
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

        assertThatThrownBy(() -> loginService.login(command()))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> assertThat(((BaseException) ex).getErrorCode()).isEqualTo(UserErrorCode.LOGIN_FAILED));

        verify(jwtProvider, never()).createAccessToken(any(), any());
    }

    @Test
    @DisplayName("정지된 계정이면 비밀번호가 일치해도 ACCOUNT_SUSPENDED 예외를 던진다")
    void login_suspendedAccount() {
        User user = activeUser();
        ReflectionTestUtils.setField(user, "status", UserStatus.SUSPENDED);
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

        assertThatThrownBy(() -> loginService.login(command()))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> assertThat(((BaseException) ex).getErrorCode()).isEqualTo(UserErrorCode.ACCOUNT_SUSPENDED));

        verify(jwtProvider, never()).createAccessToken(any(), any());
    }
}
