package com.workoutdone.rpgym.user.application.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpCommand {

    private String email;
    private String rawPassword;
    private String nickname;
    private String slackId;
}
