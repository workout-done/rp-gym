package com.workoutdone.rpgym.user.user.application;

import com.workoutdone.rpgym.common.exception.BaseException;
import com.workoutdone.rpgym.user.user.domain.User;
import com.workoutdone.rpgym.user.user.domain.UserErrorCode;
import com.workoutdone.rpgym.user.user.domain.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetMyAccountServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetMyAccountService getMyAccountService;

    private User activeUser(UUID id) {
        User user = User.create("healthuser@example.com", "encoded-password", "헬스퀘스트유저", "U0123ABC456");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    @DisplayName("활성 계정이면 계정 정보를 반환한다")
    void getMyAccount_success() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId);
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));

        GetMyAccountResult result = getMyAccountService.getMyAccount(userId);

        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getEmail()).isEqualTo("healthuser@example.com");
        assertThat(result.getNickname()).isEqualTo("헬스퀘스트유저");
        assertThat(result.getSlackId()).isEqualTo("U0123ABC456");
        assertThat(result.getRole().name()).isEqualTo("USER");
        assertThat(result.getStatus().name()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("존재하지 않는 id면 USER_NOT_FOUND 예외를 던진다")
    void getMyAccount_notFound() {
        UUID userId = UUID.randomUUID();
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> getMyAccountService.getMyAccount(userId))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> assertThat(((BaseException) ex).getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND));
    }

    @Test
    @DisplayName("탈퇴한 계정이면 USER_NOT_FOUND 예외를 던진다")
    void getMyAccount_withdrawnAccount() {
        // findByIdAndDeletedAtIsNull은 deletedAt이 있는 계정을 애초에 조회하지 않으므로
        // Repository가 Optional.empty()를 반환하는 것으로 시뮬레이션한다.
        UUID userId = UUID.randomUUID();
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> getMyAccountService.getMyAccount(userId))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> assertThat(((BaseException) ex).getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND));
    }
}
