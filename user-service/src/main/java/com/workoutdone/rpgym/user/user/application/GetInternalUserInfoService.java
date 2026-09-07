package com.workoutdone.rpgym.user.user.application;

import com.workoutdone.rpgym.common.exception.BaseException;
import com.workoutdone.rpgym.user.user.domain.User;
import com.workoutdone.rpgym.user.user.domain.UserErrorCode;
import com.workoutdone.rpgym.user.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetInternalUserInfoService {

    private final UserRepository userRepository;

    public GetInternalUserInfoResult getUserInfo(UUID userId) {
        // 탈퇴한 사용자도 404 없이 정상 응답해야 하므로 deletedAt 조건 없이 조회함
        // (User의 getDisplayStatus()로 사용자의 계정상태 확인 가능)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

        return GetInternalUserInfoResult.from(user);
    }
}
