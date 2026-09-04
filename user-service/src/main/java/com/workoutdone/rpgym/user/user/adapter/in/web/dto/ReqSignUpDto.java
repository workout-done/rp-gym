package com.workoutdone.rpgym.user.user.adapter.in.web.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.workoutdone.rpgym.user.jackson.TrimmingStringDeserializer;
import com.workoutdone.rpgym.user.user.application.SignUpCommand;
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

    @JsonDeserialize(using = TrimmingStringDeserializer.class)
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 255, message = "이메일은 최대 255자까지 입력 가능합니다.")
    private String email;

    // 비밀번호는 트리밍하지 않는다. 앞뒤 공백도 사용자가 입력한 비밀번호의 일부다.
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, max = 72, message = "비밀번호는 8자 이상 72자 이하로 입력해주세요.")
    private String password;

    @JsonDeserialize(using = TrimmingStringDeserializer.class)
    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(max = 50, message = "닉네임은 최대 50자까지 입력 가능합니다.")
    private String nickname;

    @JsonDeserialize(using = TrimmingStringDeserializer.class)
    @NotBlank(message = "슬랙 아이디를 입력해주세요.")
    @Size(max = 100, message = "슬랙 아이디는 최대 100자까지 입력 가능합니다.")
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
