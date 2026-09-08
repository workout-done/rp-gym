package com.workoutdone.rpgym.user.internal.application;

import com.workoutdone.rpgym.common.exception.BaseException;
import com.workoutdone.rpgym.user.dailyhealthgoal.domain.DailyHealthGoalRepository;
import com.workoutdone.rpgym.user.healthprofile.domain.HealthProfileRepository;
import com.workoutdone.rpgym.user.user.domain.UserErrorCode;
import com.workoutdone.rpgym.user.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserHealthContextService {

    private final UserRepository userRepository;
    private final HealthProfileRepository healthProfileRepository;
    private final DailyHealthGoalRepository dailyHealthGoalRepository;

    public GetUserHealthContextResult getHealthContext(UUID userId) {
        // 탈퇴한 사용자는 건강 데이터 계산 대상이 아니므로 존재하지 않는 것과 동일하게 취급
        userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

        // 미등록 시에는 null을 그대로 응답한다(Health Service가 처리해야 하는 정상 상태).
        HealthProfileSummary healthProfile = healthProfileRepository.findByUserIdAndDeletedAtIsNull(userId)
                .map(HealthProfileSummary::from)
                .orElse(null);

        DailyGoalSummary dailyGoal = dailyHealthGoalRepository.findByUserIdAndDeletedAtIsNull(userId)
                .map(DailyGoalSummary::from)
                .orElse(null);

        ////TO-DO: 장기 목표 기능이 추후에 구현되면 status='IN_PROGRESS'인 목표 목록으로 채우고
        ////        그 전까진 임시로 null 로 채운다.
        List<LongTermGoalSummary> longTermGoals = null;

        return GetUserHealthContextResult.builder()
                .healthProfile(healthProfile)
                .dailyGoal(dailyGoal)
                .longTermGoals(longTermGoals)
                .build();
    }
}
