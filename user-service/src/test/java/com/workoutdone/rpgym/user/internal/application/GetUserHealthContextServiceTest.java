package com.workoutdone.rpgym.user.internal.application;

import com.workoutdone.rpgym.common.exception.BaseException;
import com.workoutdone.rpgym.user.dailyhealthgoal.domain.DailyHealthGoal;
import com.workoutdone.rpgym.user.dailyhealthgoal.domain.DailyHealthGoalRepository;
import com.workoutdone.rpgym.user.healthprofile.domain.HealthProfile;
import com.workoutdone.rpgym.user.healthprofile.domain.HealthProfileRepository;
import com.workoutdone.rpgym.user.user.domain.User;
import com.workoutdone.rpgym.user.user.domain.UserErrorCode;
import com.workoutdone.rpgym.user.user.domain.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GetUserHealthContextServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HealthProfileRepository healthProfileRepository;

    @Mock
    private DailyHealthGoalRepository dailyHealthGoalRepository;

    @InjectMocks
    private GetUserHealthContextService getUserHealthContextService;

    private User activeUser() {
        return User.create("healthuser@example.com", "encoded-password", "헬스퀘스트유저", "U0123ABC456");
    }

    @Test
    @DisplayName("바디 프로필과 일일 목표가 모두 등록돼 있으면 둘 다 채워서 반환한다")
    void getHealthContext_bothRegistered_returnsBothFilled() {
        UUID userId = UUID.randomUUID();
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(activeUser()));
        given(healthProfileRepository.findByUserIdAndDeletedAtIsNull(userId))
                .willReturn(Optional.of(HealthProfile.create(userId, BigDecimal.valueOf(170.5), BigDecimal.valueOf(65.2))));
        given(dailyHealthGoalRepository.findByUserIdAndDeletedAtIsNull(userId))
                .willReturn(Optional.of(DailyHealthGoal.create(userId, 5000, 60, 500)));

        GetUserHealthContextResult result = getUserHealthContextService.getHealthContext(userId);

        assertThat(result.getHealthProfile().getHeight()).isEqualByComparingTo(BigDecimal.valueOf(170.5));
        assertThat(result.getHealthProfile().getWeight()).isEqualByComparingTo(BigDecimal.valueOf(65.2));
        assertThat(result.getDailyGoal().getStepGoal()).isEqualTo(5000);
        assertThat(result.getDailyGoal().getActiveMinutesGoal()).isEqualTo(60);
        assertThat(result.getDailyGoal().getActiveCaloriesGoal()).isEqualTo(500);
        assertThat(result.getLongTermGoals()).isNull();
    }

    @Test
    @DisplayName("바디 프로필이 미등록이면 healthProfile은 null로, dailyGoal은 채워서 반환한다")
    void getHealthContext_healthProfileNotRegistered_returnsNullHealthProfile() {
        UUID userId = UUID.randomUUID();
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(activeUser()));
        given(healthProfileRepository.findByUserIdAndDeletedAtIsNull(userId)).willReturn(Optional.empty());
        given(dailyHealthGoalRepository.findByUserIdAndDeletedAtIsNull(userId))
                .willReturn(Optional.of(DailyHealthGoal.create(userId, 5000, 60, 500)));

        GetUserHealthContextResult result = getUserHealthContextService.getHealthContext(userId);

        assertThat(result.getHealthProfile()).isNull();
        assertThat(result.getDailyGoal()).isNotNull();
    }

    @Test
    @DisplayName("일일 목표가 미등록이면 dailyGoal은 null로, healthProfile은 채워서 반환한다")
    void getHealthContext_dailyGoalNotRegistered_returnsNullDailyGoal() {
        UUID userId = UUID.randomUUID();
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(activeUser()));
        given(healthProfileRepository.findByUserIdAndDeletedAtIsNull(userId))
                .willReturn(Optional.of(HealthProfile.create(userId, BigDecimal.valueOf(170.5), BigDecimal.valueOf(65.2))));
        given(dailyHealthGoalRepository.findByUserIdAndDeletedAtIsNull(userId)).willReturn(Optional.empty());

        GetUserHealthContextResult result = getUserHealthContextService.getHealthContext(userId);

        assertThat(result.getHealthProfile()).isNotNull();
        assertThat(result.getDailyGoal()).isNull();
    }

    @Test
    @DisplayName("둘 다 미등록이면 healthProfile/dailyGoal 모두 null로 반환한다")
    void getHealthContext_neitherRegistered_returnsBothNull() {
        UUID userId = UUID.randomUUID();
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(activeUser()));
        given(healthProfileRepository.findByUserIdAndDeletedAtIsNull(userId)).willReturn(Optional.empty());
        given(dailyHealthGoalRepository.findByUserIdAndDeletedAtIsNull(userId)).willReturn(Optional.empty());

        GetUserHealthContextResult result = getUserHealthContextService.getHealthContext(userId);

        assertThat(result.getHealthProfile()).isNull();
        assertThat(result.getDailyGoal()).isNull();
        assertThat(result.getLongTermGoals()).isNull();
    }

    @Test
    @DisplayName("존재하지 않거나 탈퇴한 사용자면 USER_NOT_FOUND 예외를 던지고 다른 조회는 하지 않는다")
    void getHealthContext_userNotFound_throwsException() {
        UUID userId = UUID.randomUUID();
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> getUserHealthContextService.getHealthContext(userId))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> assertThat(((BaseException) ex).getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND));

        verify(healthProfileRepository, never()).findByUserIdAndDeletedAtIsNull(any());
        verify(dailyHealthGoalRepository, never()).findByUserIdAndDeletedAtIsNull(any());
    }
}
