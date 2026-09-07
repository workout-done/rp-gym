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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetInternalUserInfoServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetInternalUserInfoService getInternalUserInfoService;

    private User activeUser(UUID id) {
        User user = User.create("healthuser@example.com", "encoded-password", "헬스퀘스트유저", "U0123ABC456");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    @DisplayName("활성 계정이면 id/nickname/role/status/slackId를 반환한다")
    void getUserInfo_success() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        GetInternalUserInfoResult result = getInternalUserInfoService.getUserInfo(userId);

        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getNickname()).isEqualTo("헬스퀘스트유저");
        assertThat(result.getRole().name()).isEqualTo("USER");
        assertThat(result.getStatus().name()).isEqualTo("ACTIVE");
        assertThat(result.getSlackId()).isEqualTo("U0123ABC456");
    }

    @Test
    @DisplayName("탈퇴한 계정도 404 없이 조회되고 status가 WITHDRAWN으로 내려간다")
    void getUserInfo_withdrawnAccount_returnsWithdrawnStatus() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId);
        // status 컬럼은 ACTIVE로 남아있어도 deletedAt만으로 WITHDRAWN이 파생되는지 검증한다.
        ReflectionTestUtils.setField(user, "deletedAt", LocalDateTime.now());
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        GetInternalUserInfoResult result = getInternalUserInfoService.getUserInfo(userId);

        assertThat(result.getStatus().name()).isEqualTo("WITHDRAWN");
    }

    @Test
    @DisplayName("존재하지 않는 userId면 USER_NOT_FOUND 예외를 던진다")
    void getUserInfo_notFound() {
        UUID userId = UUID.randomUUID();
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> getInternalUserInfoService.getUserInfo(userId))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> assertThat(((BaseException) ex).getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND));
    }
}
