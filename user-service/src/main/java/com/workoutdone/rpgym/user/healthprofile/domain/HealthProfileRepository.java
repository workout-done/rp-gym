package com.workoutdone.rpgym.user.healthprofile.domain;

import java.util.UUID;

public interface HealthProfileRepository {

    HealthProfile save(HealthProfile healthProfile);

    // 유니크 제약 위반을 트랜잭션 커밋 시점이 아니라 이 호출 시점에 바로 확인하기 위해 사용
    HealthProfile saveAndFlush(HealthProfile healthProfile);

    boolean existsByUserIdAndDeletedAtIsNull(UUID userId);
}
