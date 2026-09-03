package com.workoutdone.rpgym.user.application.service;

import com.workoutdone.rpgym.common.exception.BaseException;
import com.workoutdone.rpgym.user.application.input.SignUpCommand;
import com.workoutdone.rpgym.user.application.output.SignUpResult;
import com.workoutdone.rpgym.user.domain.entity.User;
import com.workoutdone.rpgym.user.domain.exception.UserErrorCode;
import com.workoutdone.rpgym.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SignUpServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SignUpService signUpService;

    private SignUpCommand command() {
        return SignUpCommand.builder()
                .email("healthuser@example.com")
                .rawPassword("myPassw0rd!")
                .nickname("헬스퀘스트유저")
                .slackId("U0123ABC456")
                .build();
    }

    @Test
    @DisplayName("이메일/닉네임이 중복되지 않으면 비밀번호를 해싱해 회원을 저장하고 결과를 반환한다")
    void signUp_success() {
        SignUpCommand command = command();
        given(userRepository.existsByEmailAndDeletedAtIsNull(command.getEmail())).willReturn(false);
        given(userRepository.existsByNicknameAndDeletedAtIsNull(command.getNickname())).willReturn(false);
        given(passwordEncoder.encode(command.getRawPassword())).willReturn("encoded-password");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        SignUpResult result = signUpService.signUp(command);

        assertThat(result.getEmail()).isEqualTo(command.getEmail());
        assertThat(result.getNickname()).isEqualTo(command.getNickname());
        assertThat(result.getRole().name()).isEqualTo("USER");
        assertThat(result.getStatus().name()).isEqualTo("ACTIVE");
        verify(passwordEncoder).encode(command.getRawPassword());
    }

    @Test
    @DisplayName("이미 사용 중인 이메일이면 EMAIL_DUPLICATED 예외를 던지고 저장하지 않는다")
    void signUp_duplicateEmail() {
        SignUpCommand command = command();
        given(userRepository.existsByEmailAndDeletedAtIsNull(command.getEmail())).willReturn(true);

        assertThatThrownBy(() -> signUpService.signUp(command))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> assertThat(((BaseException) ex).getErrorCode()).isEqualTo(UserErrorCode.EMAIL_DUPLICATED));

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("이메일은 중복이 아니지만 닉네임이 중복이면 NICKNAME_DUPLICATED 예외를 던지고 저장하지 않는다")
    void signUp_duplicateNickname() {
        SignUpCommand command = command();
        given(userRepository.existsByEmailAndDeletedAtIsNull(command.getEmail())).willReturn(false);
        given(userRepository.existsByNicknameAndDeletedAtIsNull(command.getNickname())).willReturn(true);

        assertThatThrownBy(() -> signUpService.signUp(command))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> assertThat(((BaseException) ex).getErrorCode()).isEqualTo(UserErrorCode.NICKNAME_DUPLICATED));

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }
}
