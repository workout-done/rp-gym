package com.workoutdone.rpgym.user.user.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMyAccountCommand {

    private UUID userId;
    private String nickname;
    private String slackId;
    private String currentPassword;
    private String newPassword;
}
