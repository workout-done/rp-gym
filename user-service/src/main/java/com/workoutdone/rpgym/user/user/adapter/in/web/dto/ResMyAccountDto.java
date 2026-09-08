package com.workoutdone.rpgym.user.user.adapter.in.web.dto;

import com.workoutdone.rpgym.user.user.application.GetMyAccountResult;
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
public class ResMyAccountDto {

    private UUID id;
    private String email;
    private String nickname;
    private String role;
    private String status;
    private String slackId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ResMyAccountDto from(GetMyAccountResult result) {
        return ResMyAccountDto.builder()
                .id(result.getId())
                .email(result.getEmail())
                .nickname(result.getNickname())
                .role(result.getRole().name())
                .status(result.getStatus().name())
                .slackId(result.getSlackId())
                .createdAt(result.getCreatedAt())
                .updatedAt(result.getUpdatedAt())
                .build();
    }
}
