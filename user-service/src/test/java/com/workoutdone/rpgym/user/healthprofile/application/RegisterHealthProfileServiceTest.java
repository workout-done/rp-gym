package com.workoutdone.rpgym.user.healthprofile.application;

import com.workoutdone.rpgym.common.exception.BaseException;
import com.workoutdone.rpgym.user.healthprofile.domain.HealthProfile;
import com.workoutdone.rpgym.user.healthprofile.domain.HealthProfileErrorCode;
import com.workoutdone.rpgym.user.healthprofile.domain.HealthProfileRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegisterHealthProfileServiceTest {

    @Mock
    private HealthProfileRepository healthProfileRepository;

    @InjectMocks
    private RegisterHealthProfileService registerHealthProfileService;

    private RegisterHealthProfileCommand command(UUID userId) {
        return RegisterHealthProfileCommand.builder()
                .userId(userId)
                .height(BigDecimal.valueOf(170.5))
                .weight(BigDecimal.valueOf(65.2))
                .build();
    }

    @Test
    @DisplayName("활성 프로필이 없으면 바디 프로필을 저장하고 결과를 반환한다")
    void registerHealthProfile_success() {
        UUID userId = UUID.randomUUID();
        RegisterHealthProfileCommand command = command(userId);
        given(healthProfileRepository.existsByUserIdAndDeletedAtIsNull(userId)).willReturn(false);
        given(healthProfileRepository.saveAndFlush(any(HealthProfile.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        RegisterHealthProfileResult result = registerHealthProfileService.registerHealthProfile(command);

        assertThat(result.getHeight()).isEqualByComparingTo(command.getHeight());
        assertThat(result.getWeight()).isEqualByComparingTo(command.getWeight());
    }

    @Test
    @DisplayName("이미 활성 프로필이 있으면 HEALTH_PROFILE_ALREADY_EXISTS 예외를 던지고 저장하지 않는다")
    void registerHealthProfile_alreadyExists() {
        UUID userId = UUID.randomUUID();
        given(healthProfileRepository.existsByUserIdAndDeletedAtIsNull(userId)).willReturn(true);

        assertThatThrownBy(() -> registerHealthProfileService.registerHealthProfile(command(userId)))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> assertThat(((BaseException) ex).getErrorCode()).isEqualTo(HealthProfileErrorCode.HEALTH_PROFILE_ALREADY_EXISTS));

        verify(healthProfileRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("사전 확인은 통과했지만 동시 요청으로 유니크 제약이 위반되면 HEALTH_PROFILE_ALREADY_EXISTS로 변환한다")
    void registerHealthProfile_uniqueConstraintViolatedConcurrently_convertsToAlreadyExists() {
        UUID userId = UUID.randomUUID();
        given(healthProfileRepository.existsByUserIdAndDeletedAtIsNull(userId)).willReturn(false);
        given(healthProfileRepository.saveAndFlush(any(HealthProfile.class)))
                .willThrow(dataIntegrityViolationException("ux_user_health_profiles_user_id"));

        assertThatThrownBy(() -> registerHealthProfileService.registerHealthProfile(command(userId)))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> assertThat(((BaseException) ex).getErrorCode()).isEqualTo(HealthProfileErrorCode.HEALTH_PROFILE_ALREADY_EXISTS));
    }

    @Test
    @DisplayName("유니크 제약 위반 예외가 한 단계 더 감싸져 들어와도 HEALTH_PROFILE_ALREADY_EXISTS로 변환한다")
    void registerHealthProfile_uniqueConstraintDeeplyWrapped_convertsToAlreadyExists() {
        UUID userId = UUID.randomUUID();
        given(healthProfileRepository.existsByUserIdAndDeletedAtIsNull(userId)).willReturn(false);
        given(healthProfileRepository.saveAndFlush(any(HealthProfile.class)))
                .willThrow(deeplyWrappedDataIntegrityViolationException("ux_user_health_profiles_user_id"));

        assertThatThrownBy(() -> registerHealthProfileService.registerHealthProfile(command(userId)))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> assertThat(((BaseException) ex).getErrorCode()).isEqualTo(HealthProfileErrorCode.HEALTH_PROFILE_ALREADY_EXISTS));
    }

    @Test
    @DisplayName("예상하지 못한 제약조건 위반이면 변환하지 않고 원래 예외를 그대로 던진다")
    void registerHealthProfile_unexpectedConstraintViolation_rethrowsOriginalException() {
        UUID userId = UUID.randomUUID();
        given(healthProfileRepository.existsByUserIdAndDeletedAtIsNull(userId)).willReturn(false);
        DataIntegrityViolationException unexpected = dataIntegrityViolationException("some_other_constraint");
        given(healthProfileRepository.saveAndFlush(any(HealthProfile.class))).willThrow(unexpected);

        assertThatThrownBy(() -> registerHealthProfileService.registerHealthProfile(command(userId)))
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
