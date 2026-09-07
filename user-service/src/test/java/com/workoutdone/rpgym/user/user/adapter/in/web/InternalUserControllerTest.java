package com.workoutdone.rpgym.user.user.adapter.in.web;

import com.workoutdone.rpgym.common.exception.BaseException;
import com.workoutdone.rpgym.user.user.application.GetInternalUserInfoResult;
import com.workoutdone.rpgym.user.user.application.GetInternalUserInfoService;
import com.workoutdone.rpgym.user.user.domain.UserErrorCode;
import com.workoutdone.rpgym.user.user.domain.UserRole;
import com.workoutdone.rpgym.user.user.domain.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalUserController.class)
class InternalUserControllerTest {

    private static final String URL = "/api/v1/internal/users/{userId}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetInternalUserInfoService getInternalUserInfoService;

    @Test
    @DisplayName("정상 조회 시 200과 함께 id/nickname/role/status/slackId를 반환한다")
    void getUserInfo_success() throws Exception {
        UUID userId = UUID.randomUUID();
        GetInternalUserInfoResult result = GetInternalUserInfoResult.builder()
                .id(userId)
                .nickname("헬스퀘스트유저")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .slackId("U0123ABC456")
                .build();
        given(getInternalUserInfoService.getUserInfo(any())).willReturn(result);

        mockMvc.perform(get(URL, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.nickname").value("헬스퀘스트유저"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.slackId").value("U0123ABC456"))
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    @DisplayName("탈퇴한 계정이면 404 없이 200과 함께 status=WITHDRAWN을 반환한다")
    void getUserInfo_withdrawnAccount_returnsOkWithWithdrawnStatus() throws Exception {
        UUID userId = UUID.randomUUID();
        GetInternalUserInfoResult result = GetInternalUserInfoResult.builder()
                .id(userId)
                .nickname("헬스퀘스트유저")
                .role(UserRole.USER)
                .status(UserStatus.WITHDRAWN)
                .slackId("U0123ABC456")
                .build();
        given(getInternalUserInfoService.getUserInfo(any())).willReturn(result);

        mockMvc.perform(get(URL, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WITHDRAWN"));
    }

    @Test
    @DisplayName("존재하지 않는 userId면 404 USER_NOT_FOUND를 반환한다")
    void getUserInfo_notFound() throws Exception {
        UUID userId = UUID.randomUUID();
        given(getInternalUserInfoService.getUserInfo(any()))
                .willThrow(new BaseException(UserErrorCode.USER_NOT_FOUND));

        mockMvc.perform(get(URL, userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }
}
