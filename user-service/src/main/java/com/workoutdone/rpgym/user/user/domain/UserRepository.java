package com.workoutdone.rpgym.user.user.domain;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    // 탈퇴/정지 계정도 구분해서 안내해야 하므로 deletedAt 조건 없이 조회
    Optional<User> findByEmail(String email);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    boolean existsByNicknameAndDeletedAtIsNull(String nickname);
}
