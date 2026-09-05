package com.workoutdone.rpgym.user.user.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginCommand {

    private String email;
    private String rawPassword;
}
