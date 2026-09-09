package com.workoutdone.rpgym.user.user.adapter.in.web.dto;

import com.workoutdone.rpgym.user.user.application.UpdateMyAccountResult;
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
public class ResUpdateMyAccountDto {

    private UUID id;
    private String email;
    private String nickname;
    private String role;
    private String status;
    private String slackId;
    private LocalDateTime updatedAt;

    public static ResUpdateMyAccountDto from(UpdateMyAccountResult result) {
        return ResUpdateMyAccountDto.builder()
                .id(result.getId())
                .email(result.getEmail())
                .nickname(result.getNickname())
                .role(result.getRole().name())
                .status(result.getStatus().name())
                .slackId(result.getSlackId())
                .updatedAt(result.getUpdatedAt())
                .build();
    }
}
