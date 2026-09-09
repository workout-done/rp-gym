package com.workoutdone.rpgym.user.user.application;

import com.workoutdone.rpgym.common.exception.BaseException;
import com.workoutdone.rpgym.common.exception.CommonErrorCode;
import com.workoutdone.rpgym.user.user.domain.User;
import com.workoutdone.rpgym.user.user.domain.UserErrorCode;
import com.workoutdone.rpgym.user.user.domain.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UpdateMyAccountServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UpdateMyAccountService updateMyAccountService;

    private User activeUser(UUID id) {
        User user = User.create("healthuser@example.com", "encoded-current-password", "헬스퀘스트유저", "U0123ABC456");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private UpdateMyAccountCommand command(UUID userId, String nickname, String slackId, String currentPassword, String newPassword) {
        return UpdateMyAccountCommand.builder()
                .userId(userId)
                .nickname(nickname)
                .slackId(slackId)
                .currentPassword(currentPassword)
                .newPassword(newPassword)
                .build();
    }

    @Test
    @DisplayName("닉네임만 요청하면 닉네임만 변경하고 다른 필드는 유지한다")
    void updateMyAccount_updatesNicknameOnly() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId);
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));
        given(userRepository.existsByNicknameAndIdNotAndDeletedAtIsNull("새닉네임", userId)).willReturn(false);
        given(userRepository.saveAndFlush(user)).willReturn(user);

        UpdateMyAccountResult result = updateMyAccountService.updateMyAccount(command(userId, "새닉네임", null, null, null));

        assertThat(result.getNickname()).isEqualTo("새닉네임");
        assertThat(result.getSlackId()).isEqualTo("U0123ABC456");
    }

    @Test
    @DisplayName("Slack ID만 요청하면 Slack ID만 변경한다")
    void updateMyAccount_updatesSlackIdOnly() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId);
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));
        given(userRepository.saveAndFlush(user)).willReturn(user);

        UpdateMyAccountResult result = updateMyAccountService.updateMyAccount(command(userId, null, "UNEWSLACK1", null, null));

        assertThat(result.getSlackId()).isEqualTo("UNEWSLACK1");
        assertThat(result.getNickname()).isEqualTo("헬스퀘스트유저");
        verify(userRepository, never()).existsByNicknameAndIdNotAndDeletedAtIsNull(any(), any());
    }

    @Test
    @DisplayName("currentPassword가 일치하면 새 비밀번호로 변경한다")
    void updateMyAccount_updatesPassword_whenCurrentPasswordMatches() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId);
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("myPassw0rd!", user.getPassword())).willReturn(true);
        given(passwordEncoder.encode("newPassw0rd!")).willReturn("encoded-new-password");
        given(userRepository.saveAndFlush(user)).willReturn(user);

        updateMyAccountService.updateMyAccount(command(userId, null, null, "myPassw0rd!", "newPassw0rd!"));

        assertThat(user.getPassword()).isEqualTo("encoded-new-password");
    }

    @Test
    @DisplayName("currentPassword만 있고 newPassword가 없으면 비밀번호 변경 없이 무시한다")
    void updateMyAccount_currentPasswordOnlyWithoutNewPassword_ignoresPasswordChange() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId);
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));
        given(userRepository.saveAndFlush(user)).willReturn(user);

        updateMyAccountService.updateMyAccount(command(userId, null, null, "myPassw0rd!", null));

        assertThat(user.getPassword()).isEqualTo("encoded-current-password");
        verify(passwordEncoder, never()).matches(any(), any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("활성 계정 중 이미 사용 중인 닉네임이면 NICKNAME_DUPLICATED 예외를 던지고 저장하지 않는다")
    void updateMyAccount_nicknameDuplicated_throwsNicknameDuplicated() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId);
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));
        given(userRepository.existsByNicknameAndIdNotAndDeletedAtIsNull("중복닉네임", userId)).willReturn(true);

        assertThatThrownBy(() -> updateMyAccountService.updateMyAccount(command(userId, "중복닉네임", null, null, null)))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> assertThat(((BaseException) ex).getErrorCode()).isEqualTo(UserErrorCode.NICKNAME_DUPLICATED));

        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("newPassword만 있고 currentPassword가 없으면 INVALID_INPUT 예외를 던진다")
    void updateMyAccount_newPasswordWithoutCurrentPassword_throwsInvalidInput() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId);
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> updateMyAccountService.updateMyAccount(command(userId, null, null, null, "newPassw0rd!")))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> assertThat(((BaseException) ex).getErrorCode()).isEqualTo(CommonErrorCode.INVALID_INPUT));

        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("currentPassword가 실제 비밀번호와 다르면 LOGIN_FAILED 예외를 던진다")
    void updateMyAccount_currentPasswordDoesNotMatch_throwsLoginFailed() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId);
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongPassword!", user.getPassword())).willReturn(false);

        assertThatThrownBy(() -> updateMyAccountService.updateMyAccount(command(userId, null, null, "wrongPassword!", "newPassw0rd!")))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> assertThat(((BaseException) ex).getErrorCode()).isEqualTo(UserErrorCode.LOGIN_FAILED));

        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("존재하지 않거나 탈퇴한 사용자면 USER_NOT_FOUND 예외를 던진다")
    void updateMyAccount_userNotFound_throwsUserNotFound() {
        UUID userId = UUID.randomUUID();
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> updateMyAccountService.updateMyAccount(command(userId, "닉네임", null, null, null)))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> assertThat(((BaseException) ex).getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND));
    }

    @Test
    @DisplayName("사전 확인은 통과했지만 동시 요청으로 닉네임 유니크 제약이 위반되면 NICKNAME_DUPLICATED로 변환한다")
    void updateMyAccount_nicknameUniqueConstraintViolatedConcurrently_convertsToNicknameDuplicated() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId);
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));
        given(userRepository.existsByNicknameAndIdNotAndDeletedAtIsNull("새닉네임", userId)).willReturn(false);
        given(userRepository.saveAndFlush(user))
                .willThrow(dataIntegrityViolationException("ux_users_nickname_active"));

        assertThatThrownBy(() -> updateMyAccountService.updateMyAccount(command(userId, "새닉네임", null, null, null)))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> assertThat(((BaseException) ex).getErrorCode()).isEqualTo(UserErrorCode.NICKNAME_DUPLICATED));
    }

    @Test
    @DisplayName("유니크 제약 위반 예외가 한 단계 더 감싸져 들어와도 NICKNAME_DUPLICATED로 변환한다")
    void updateMyAccount_uniqueConstraintDeeplyWrapped_convertsToNicknameDuplicated() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId);
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));
        given(userRepository.existsByNicknameAndIdNotAndDeletedAtIsNull("새닉네임", userId)).willReturn(false);
        given(userRepository.saveAndFlush(user))
                .willThrow(deeplyWrappedDataIntegrityViolationException("ux_users_nickname_active"));

        assertThatThrownBy(() -> updateMyAccountService.updateMyAccount(command(userId, "새닉네임", null, null, null)))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> assertThat(((BaseException) ex).getErrorCode()).isEqualTo(UserErrorCode.NICKNAME_DUPLICATED));
    }

    @Test
    @DisplayName("예상하지 못한 제약조건 위반이면 변환하지 않고 원래 예외를 그대로 던진다")
    void updateMyAccount_unexpectedConstraintViolation_rethrowsOriginalException() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId);
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));
        given(userRepository.existsByNicknameAndIdNotAndDeletedAtIsNull("새닉네임", userId)).willReturn(false);
        DataIntegrityViolationException unexpected = dataIntegrityViolationException("some_other_constraint");
        given(userRepository.saveAndFlush(user)).willThrow(unexpected);

        assertThatThrownBy(() -> updateMyAccountService.updateMyAccount(command(userId, "새닉네임", null, null, null)))
                .isSameAs(unexpected);
    }

    // DB가 부분 유니크 제약을 위반했을 때 Hibernate/Spring이 실제로 던지는 예외 형태를 그대로 흉내낸다.
    private DataIntegrityViolationException dataIntegrityViolationException(String constraintName) {
        ConstraintViolationException cause = new ConstraintViolationException(
                "duplicate key value violates unique constraint",
                new SQLException("duplicate key"),
                constraintName
        );
        return new DataIntegrityViolationException("could not execute statement", cause);
    }

    // 드라이버/커넥션 풀 등에 의해 ConstraintViolationException이 한 단계 더 감싸져 들어오는 상황을 흉내낸다.
    private DataIntegrityViolationException deeplyWrappedDataIntegrityViolationException(String constraintName) {
        ConstraintViolationException cve = new ConstraintViolationException(
                "duplicate key value violates unique constraint",
                new SQLException("duplicate key"),
                constraintName
        );
        RuntimeException extraWrapper = new RuntimeException("한 단계 더 감싸진 상황을 흉내냄", cve);
        return new DataIntegrityViolationException("could not execute statement", extraWrapper);
    }
}
