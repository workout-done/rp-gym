package com.workoutdone.rpgym.user.application.output;

import com.workoutdone.rpgym.user.domain.entity.User;
import com.workoutdone.rpgym.user.domain.entity.UserRole;
import com.workoutdone.rpgym.user.domain.entity.UserStatus;
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
public class SignUpResult {

    private UUID id;
    private String email;
    private String nickname;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;

    public static SignUpResult from(User user) {
        return SignUpResult.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
