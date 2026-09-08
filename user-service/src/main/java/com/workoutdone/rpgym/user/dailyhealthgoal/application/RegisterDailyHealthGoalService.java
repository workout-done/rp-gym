package com.workoutdone.rpgym.user.dailyhealthgoal.application;

import com.workoutdone.rpgym.common.exception.BaseException;
import com.workoutdone.rpgym.user.dailyhealthgoal.domain.DailyHealthGoal;
import com.workoutdone.rpgym.user.dailyhealthgoal.domain.DailyHealthGoalErrorCode;
import com.workoutdone.rpgym.user.dailyhealthgoal.domain.DailyHealthGoalRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RegisterDailyHealthGoalService {

    // V2__create_user_daily_health_goals_table.sql에 정의된 부분 유니크 인덱스 이름과 동일해야 한다.
    private static final String USER_ID_UNIQUE_CONSTRAINT = "ux_user_daily_health_goals_user_id";

    private final DailyHealthGoalRepository dailyHealthGoalRepository;

    public RegisterDailyHealthGoalResult registerDailyHealthGoal(RegisterDailyHealthGoalCommand command) {
        validateNotAlreadyRegistered(command.getUserId());

        DailyHealthGoal dailyHealthGoal = DailyHealthGoal.create(
                command.getUserId(),
                command.getStepGoal(),
                command.getActiveMinutesGoal(),
                command.getActiveCaloriesGoal()
        );

        // 사전 중복 확인을 통과했더라도 동시 요청으로 인해 그 사이 다른 요청이 먼저 커밋됐을 수 있음
        // 최종 방어선은 DB 부분 유니크 인덱스이며, saveAndFlush로 즉시 INSERT를 실행해서
        // (트랜잭션 커밋 시점까지 미루지 않고) 여기서 바로 위반 여부를 확인함
        DailyHealthGoal savedDailyHealthGoal;
        try {
            savedDailyHealthGoal = dailyHealthGoalRepository.saveAndFlush(dailyHealthGoal);
        } catch (DataIntegrityViolationException e) {
            throw resolveDuplicateException(e);
        }

        return RegisterDailyHealthGoalResult.from(savedDailyHealthGoal);
    }

    // 활성 목표(deletedAt이 null인 목표)가 이미 있으면 재등록을 거절
    // 삭제(soft delete) 이력이 있는 경우는 재등록 가능
    private void validateNotAlreadyRegistered(UUID userId) {
        if (dailyHealthGoalRepository.existsByUserIdAndDeletedAtIsNull(userId)) {
            throw new BaseException(DailyHealthGoalErrorCode.DAILY_GOAL_ALREADY_EXISTS);
        }
    }

    // 어떤 유니크 제약조건이 위반됐는지 확인해서 알맞은 중복 에러로 변환
    // 예상하지 못한 제약조건 위반이면 그대로 다시 던져서 500으로 처리되게 둠
    private BaseException resolveDuplicateException(DataIntegrityViolationException e) {
        ConstraintViolationException cve = findConstraintViolationException(e);

        if (cve != null) {
            if (USER_ID_UNIQUE_CONSTRAINT.equals(cve.getConstraintName())) {
                return new BaseException(DailyHealthGoalErrorCode.DAILY_GOAL_ALREADY_EXISTS);
            }
        }

        throw e;
    }

    // 드라이버/커넥션 풀 등에 의해 예외가 한 단계 이상 더 감싸져 들어오는 환경도 대비해,
    // e.getCause() 한 단계만 보지 않고 원인 체인을 끝까지 순회하며 찾는다.
    private ConstraintViolationException findConstraintViolationException(Throwable throwable) {
        Throwable cause = throwable;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException cve) {
                return cve;
            }
            cause = cause.getCause();
        }

        return null;
    }
}
