package com.workoutdone.rpgym.user.dailyhealthgoal.application;

import com.workoutdone.rpgym.common.exception.BaseException;
import com.workoutdone.rpgym.user.dailyhealthgoal.domain.DailyHealthGoal;
import com.workoutdone.rpgym.user.dailyhealthgoal.domain.DailyHealthGoalErrorCode;
import com.workoutdone.rpgym.user.dailyhealthgoal.domain.DailyHealthGoalRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegisterDailyHealthGoalServiceTest {

    @Mock
    private DailyHealthGoalRepository dailyHealthGoalRepository;

    @InjectMocks
    private RegisterDailyHealthGoalService registerDailyHealthGoalService;

    private RegisterDailyHealthGoalCommand command(UUID userId, Integer stepGoal, Integer activeMinutesGoal, Integer activeCaloriesGoal) {
        return RegisterDailyHealthGoalCommand.builder()
                .userId(userId)
                .stepGoal(stepGoal)
                .activeMinutesGoal(activeMinutesGoal)
                .activeCaloriesGoal(activeCaloriesGoal)
                .build();
    }

    @Test
    @DisplayName("활성 목표가 없으면 요청받은 값으로 일일 목표를 저장하고 결과를 반환한다")
    void registerDailyHealthGoal_success() {
        UUID userId = UUID.randomUUID();
        RegisterDailyHealthGoalCommand command = command(userId, 5000, 60, 500);
        given(dailyHealthGoalRepository.existsByUserIdAndDeletedAtIsNull(userId)).willReturn(false);
        given(dailyHealthGoalRepository.saveAndFlush(any(DailyHealthGoal.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        RegisterDailyHealthGoalResult result = registerDailyHealthGoalService.registerDailyHealthGoal(command);

        assertThat(result.getStepGoal()).isEqualTo(5000);
        assertThat(result.getActiveMinutesGoal()).isEqualTo(60);
        assertThat(result.getActiveCaloriesGoal()).isEqualTo(500);
    }

    @Test
    @DisplayName("요청에서 생략한(null) 목표값은 기본값(3000/30/300)으로 채워 저장한다")
    void registerDailyHealthGoal_appliesDefaultsForOmittedFields() {
        UUID userId = UUID.randomUUID();
        RegisterDailyHealthGoalCommand command = command(userId, null, null, null);
        given(dailyHealthGoalRepository.existsByUserIdAndDeletedAtIsNull(userId)).willReturn(false);
        given(dailyHealthGoalRepository.saveAndFlush(any(DailyHealthGoal.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        RegisterDailyHealthGoalResult result = registerDailyHealthGoalService.registerDailyHealthGoal(command);

        assertThat(result.getStepGoal()).isEqualTo(DailyHealthGoal.DEFAULT_STEP_GOAL);
        assertThat(result.getActiveMinutesGoal()).isEqualTo(DailyHealthGoal.DEFAULT_ACTIVE_MINUTES_GOAL);
        assertThat(result.getActiveCaloriesGoal()).isEqualTo(DailyHealthGoal.DEFAULT_ACTIVE_CALORIES_GOAL);
    }

    @Test
    @DisplayName("이미 활성 목표가 있으면 DAILY_GOAL_ALREADY_EXISTS 예외를 던지고 저장하지 않는다")
    void registerDailyHealthGoal_alreadyExists() {
        UUID userId = UUID.randomUUID();
        given(dailyHealthGoalRepository.existsByUserIdAndDeletedAtIsNull(userId)).willReturn(true);

        assertThatThrownBy(() -> registerDailyHealthGoalService.registerDailyHealthGoal(command(userId, null, null, null)))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> assertThat(((BaseException) ex).getErrorCode()).isEqualTo(DailyHealthGoalErrorCode.DAILY_GOAL_ALREADY_EXISTS));

        verify(dailyHealthGoalRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("사전 확인은 통과했지만 동시 요청으로 유니크 제약이 위반되면 DAILY_GOAL_ALREADY_EXISTS로 변환한다")
    void registerDailyHealthGoal_uniqueConstraintViolatedConcurrently_convertsToAlreadyExists() {
        UUID userId = UUID.randomUUID();
        given(dailyHealthGoalRepository.existsByUserIdAndDeletedAtIsNull(userId)).willReturn(false);
        given(dailyHealthGoalRepository.saveAndFlush(any(DailyHealthGoal.class)))
                .willThrow(dataIntegrityViolationException("ux_user_daily_health_goals_user_id"));

        assertThatThrownBy(() -> registerDailyHealthGoalService.registerDailyHealthGoal(command(userId, null, null, null)))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> assertThat(((BaseException) ex).getErrorCode()).isEqualTo(DailyHealthGoalErrorCode.DAILY_GOAL_ALREADY_EXISTS));
    }

    @Test
    @DisplayName("유니크 제약 위반 예외가 한 단계 더 감싸져 들어와도 DAILY_GOAL_ALREADY_EXISTS로 변환한다")
    void registerDailyHealthGoal_uniqueConstraintDeeplyWrapped_convertsToAlreadyExists() {
        UUID userId = UUID.randomUUID();
        given(dailyHealthGoalRepository.existsByUserIdAndDeletedAtIsNull(userId)).willReturn(false);
        given(dailyHealthGoalRepository.saveAndFlush(any(DailyHealthGoal.class)))
                .willThrow(deeplyWrappedDataIntegrityViolationException("ux_user_daily_health_goals_user_id"));

        assertThatThrownBy(() -> registerDailyHealthGoalService.registerDailyHealthGoal(command(userId, null, null, null)))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> assertThat(((BaseException) ex).getErrorCode()).isEqualTo(DailyHealthGoalErrorCode.DAILY_GOAL_ALREADY_EXISTS));
    }

    @Test
    @DisplayName("예상하지 못한 제약조건 위반이면 변환하지 않고 원래 예외를 그대로 던진다")
    void registerDailyHealthGoal_unexpectedConstraintViolation_rethrowsOriginalException() {
        UUID userId = UUID.randomUUID();
        given(dailyHealthGoalRepository.existsByUserIdAndDeletedAtIsNull(userId)).willReturn(false);
        DataIntegrityViolationException unexpected = dataIntegrityViolationException("some_other_constraint");
        given(dailyHealthGoalRepository.saveAndFlush(any(DailyHealthGoal.class))).willThrow(unexpected);

        assertThatThrownBy(() -> registerDailyHealthGoalService.registerDailyHealthGoal(command(userId, null, null, null)))
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
