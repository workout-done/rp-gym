package com.workoutdone.rpgym.user.healthprofile.application;

import com.workoutdone.rpgym.common.exception.BaseException;
import com.workoutdone.rpgym.user.healthprofile.domain.HealthProfile;
import com.workoutdone.rpgym.user.healthprofile.domain.HealthProfileErrorCode;
import com.workoutdone.rpgym.user.healthprofile.domain.HealthProfileRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RegisterHealthProfileService {

    // V2__create_health_profiles_table.sql에 정의된 부분 유니크 인덱스 이름과 동일해야 한다.
    private static final String USER_ID_UNIQUE_CONSTRAINT = "ux_user_health_profiles_user_id";

    private final HealthProfileRepository healthProfileRepository;

    public RegisterHealthProfileResult registerHealthProfile(RegisterHealthProfileCommand command) {
        validateNotAlreadyRegistered(command.getUserId());

        HealthProfile healthProfile = HealthProfile.create(
                command.getUserId(),
                command.getHeight(),
                command.getWeight()
        );

        // 사전 중복 확인을 통과했더라도 동시 요청으로 인해 그 사이 다른 요청이 먼저 커밋됐을 수 있음
        // 최종 방어선은 DB 부분 유니크 인덱스이며, saveAndFlush로 즉시 INSERT를 실행해서
        // (트랜잭션 커밋 시점까지 미루지 않고) 여기서 바로 위반 여부를 확인함
        HealthProfile savedHealthProfile;
        try {
            savedHealthProfile = healthProfileRepository.saveAndFlush(healthProfile);
        } catch (DataIntegrityViolationException e) {
            throw resolveDuplicateException(e);
        }

        return RegisterHealthProfileResult.from(savedHealthProfile);
    }

    // 활성 프로필(deletedAt이 null인 프로필)이 이미 있으면 재등록을 거절
    // 삭제(soft delete) 이력이 있는 경우는 재등록 가능
    private void validateNotAlreadyRegistered(UUID userId) {
        if (healthProfileRepository.existsByUserIdAndDeletedAtIsNull(userId)) {
            throw new BaseException(HealthProfileErrorCode.HEALTH_PROFILE_ALREADY_EXISTS);
        }
    }

    // 어떤 유니크 제약조건이 위반됐는지 확인해서 알맞은 중복 에러로 변환
    // 예상하지 못한 제약조건 위반이면 그대로 다시 던져서 500으로 처리되게 둠
    private BaseException resolveDuplicateException(DataIntegrityViolationException e) {
        if (e.getCause() instanceof ConstraintViolationException cve) {
            if (USER_ID_UNIQUE_CONSTRAINT.equals(cve.getConstraintName())) {
                return new BaseException(HealthProfileErrorCode.HEALTH_PROFILE_ALREADY_EXISTS);
            }
        }

        throw e;
    }
}
