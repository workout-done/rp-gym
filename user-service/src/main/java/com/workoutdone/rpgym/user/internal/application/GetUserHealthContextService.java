package com.workoutdone.rpgym.user.internal.application;

import com.workoutdone.rpgym.common.exception.BaseException;
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

    public GetUserHealthContextResult getHealthContext(UUID userId) {
        // 탈퇴한 사용자는 건강 데이터 계산 대상이 아니므로 존재하지 않는 것과 동일하게 취급
        userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

        ////TO-DO: feature/50-register-body-profile 브랜치가 머지되면 HealthProfileRepository로 활성 바디 프로필을 조회해 채운다. 미등록 시에는 지금처럼 null을 유지한다.
        ////TO-DO: 주의: feature/50과 feature/51은 로컬 개발 DB에서 서로 다른 V2 마이그레이션 파일을 갖고 있어 버전 번호가 겹친다.
        //          두 브랜치를 로컬에서 번갈아 테스트하려면 먼저 머지된 브랜치의 마이그레이션 파일명을 V3로 바꾸거나 로컬 DB의 flyway_schema_history를 리셋해야 한다.
        HealthProfileSummary healthProfile = null;

        ////TO-DO: feature/51-register-daily-goal 브랜치가 머지되면 DailyHealthGoalRepository로 활성 일일 목표를 조회해 채운다. 미등록 시에는 지금처럼 null을 유지한다.
        ////TO-DO: (버전 번호 충돌 관련 주의사항은 위 healthProfile 주석 참고)
        DailyGoalSummary dailyGoal = null;

        ////TO-DO: 장기 목표 기능이 구현되면 status='IN_PROGRESS'인 목표 목록으로 채운다.
        List<LongTermGoalSummary> longTermGoals = null;

        return GetUserHealthContextResult.builder()
                .healthProfile(healthProfile)
                .dailyGoal(dailyGoal)
                .longTermGoals(longTermGoals)
                .build();
    }
}
