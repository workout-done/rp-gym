package com.workoutdone.rpgym.user.presentation.dto.response;

import com.workoutdone.rpgym.user.application.output.SignUpResult;
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
public class ResSignUpDto {

    private UUID id;
    private String email;
    private String nickname;
    private String role;
    private String status;
    private LocalDateTime createdAt;

    public static ResSignUpDto from(SignUpResult result) {
        return ResSignUpDto.builder()
                .id(result.getId())
                .email(result.getEmail())
                .nickname(result.getNickname())
                .role(result.getRole().name())
                .status(result.getStatus().name())
                .createdAt(result.getCreatedAt())
                .build();
    }
}
