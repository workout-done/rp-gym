package com.workoutdone.rpgym.user.user.adapter.in.web.dto;

import com.workoutdone.rpgym.user.user.application.GetInternalUserInfoResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResInternalUserInfoDto {

    private UUID id;
    private String nickname;
    private String role;
    private String status;
    private String slackId;

    public static ResInternalUserInfoDto from(GetInternalUserInfoResult result) {
        return ResInternalUserInfoDto.builder()
                .id(result.getId())
                .nickname(result.getNickname())
                .role(result.getRole().name())
                .status(result.getStatus().name())
                .slackId(result.getSlackId())
                .build();
    }
}
