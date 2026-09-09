package com.workoutdone.rpgym.user.user.adapter.in.web.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.workoutdone.rpgym.user.jackson.TrimmingStringDeserializer;
import com.workoutdone.rpgym.user.user.application.UpdateMyAccountCommand;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReqUpdateMyAccountDto {

    @JsonDeserialize(using = TrimmingStringDeserializer.class)
    @Size(max = 50, message = "닉네임은 최대 50자까지 입력 가능합니다.")
    private String nickname;

    @JsonDeserialize(using = TrimmingStringDeserializer.class)
    @Size(max = 100, message = "슬랙 아이디는 최대 100자까지 입력 가능합니다.")
    private String slackId;

    // 비밀번호는 트리밍하지 않음
    // newPassword가 있을 때만 필수이므로 형식 검증은 Service에서 처리
    private String currentPassword;

    @Size(min = 8, max = 72, message = "비밀번호는 8자 이상 72자 이하로 입력해주세요.")
    private String newPassword;

    public UpdateMyAccountCommand toCommand(UUID userId) {
        return UpdateMyAccountCommand.builder()
                .userId(userId)
                .nickname(nickname)
                .slackId(slackId)
                .currentPassword(currentPassword)
                .newPassword(newPassword)
                .build();
    }
}
