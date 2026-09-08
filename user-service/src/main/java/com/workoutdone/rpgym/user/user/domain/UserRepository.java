package com.workoutdone.rpgym.user.user.domain;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    // 유니크 제약 위반, updatedAt(@LastModifiedDate) 등 auditing 값을 트랜잭션 커밋 시점이 아니라 이 호출 시점에 바로 확인하기 위해 사용
    User saveAndFlush(User user);

    Optional<User> findById(UUID id);

    // 탈퇴한 계정은 존재하지 않는 것과 동일하게 취급하기 위해 deletedAt 조건 추가
    Optional<User> findByIdAndDeletedAtIsNull(UUID id);

    // 탈퇴/정지 계정도 구분해서 안내해야 하므로 deletedAt 조건 없이 조회
    Optional<User> findByEmail(String email);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    boolean existsByNicknameAndDeletedAtIsNull(String nickname);

    // 본인의 기존 닉네임은 중복으로 취급하지 않기 위해 대상 사용자를 제외하고 확인
    boolean existsByNicknameAndIdNotAndDeletedAtIsNull(String nickname, UUID id);
}
