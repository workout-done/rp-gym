package com.workoutdone.rpgym.user.user.application;

import com.workoutdone.rpgym.user.user.domain.User;
import com.workoutdone.rpgym.user.user.domain.UserRole;
import com.workoutdone.rpgym.user.user.domain.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMyAccountResult {

    private UUID id;
    private String email;
    private String nickname;
    private UserRole role;
    private UserStatus status;
    private String slackId;
    private LocalDateTime updatedAt;

    public static UpdateMyAccountResult from(User user) {
        return UpdateMyAccountResult.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole())
                .status(user.getStatus())
                .slackId(user.getSlackId())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
