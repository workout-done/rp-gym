package com.workoutdone.rpgym.user.dailyhealthgoal.domain;

import java.util.Optional;
import java.util.UUID;

public interface DailyHealthGoalRepository {

    DailyHealthGoal save(DailyHealthGoal dailyHealthGoal);

    // 유니크 제약 위반을 트랜잭션 커밋 시점이 아니라 이 호출 시점에 바로 확인하기 위해 사용
    DailyHealthGoal saveAndFlush(DailyHealthGoal dailyHealthGoal);

    boolean existsByUserIdAndDeletedAtIsNull(UUID userId);

    // 탈퇴/삭제된 목표는 조회 대상이 아니므로 활성 목표만 조회
    Optional<DailyHealthGoal> findByUserIdAndDeletedAtIsNull(UUID userId);
}
