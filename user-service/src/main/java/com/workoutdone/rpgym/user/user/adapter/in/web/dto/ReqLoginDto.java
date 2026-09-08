package com.workoutdone.rpgym.user.user.adapter.in.web.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.workoutdone.rpgym.user.jackson.TrimmingStringDeserializer;
import com.workoutdone.rpgym.user.user.application.LoginCommand;
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
public class ReqLoginDto {

    @JsonDeserialize(using = TrimmingStringDeserializer.class)
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    // 비밀번호는 트리밍하지 않는다. 앞뒤 공백도 사용자가 입력한 비밀번호의 일부다.
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, max = 72, message = "비밀번호는 8자 이상 72자 이하로 입력해주세요.")
    private String password;

    public LoginCommand toCommand() {
        return LoginCommand.builder()
                .email(email)
                .rawPassword(password)
                .build();
    }
}
