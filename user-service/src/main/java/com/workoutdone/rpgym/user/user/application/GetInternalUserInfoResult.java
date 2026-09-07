package com.workoutdone.rpgym.user.user.application;

import com.workoutdone.rpgym.user.user.domain.User;
import com.workoutdone.rpgym.user.user.domain.UserRole;
import com.workoutdone.rpgym.user.user.domain.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetInternalUserInfoResult {

    private UUID id;
    private String nickname;
    private UserRole role;
    private UserStatus status;
    private String slackId;

    public static GetInternalUserInfoResult from(User user) {
        return GetInternalUserInfoResult.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .role(user.getRole())
                .status(user.getDisplayStatus())
                .slackId(user.getSlackId())
                .build();
    }
}
