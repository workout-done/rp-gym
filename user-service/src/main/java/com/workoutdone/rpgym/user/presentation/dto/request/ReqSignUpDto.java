package com.workoutdone.rpgym.user.presentation.dto.request;

import com.workoutdone.rpgym.user.application.input.SignUpCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReqSignUpDto {

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @NotBlank
    @Size(min = 8, max = 64)
    private String password;

    @NotBlank
    @Size(max = 50)
    private String nickname;

    @NotBlank
    @Size(max = 100)
    private String slackId;

    public SignUpCommand toCommand() {
        return SignUpCommand.builder()
                .email(email)
                .rawPassword(password)
                .nickname(nickname)
                .slackId(slackId)
                .build();
    }
}
