package com.workoutdone.rpgym.user.internal.adapter.in.web;

import com.workoutdone.rpgym.common.exception.BaseException;
import com.workoutdone.rpgym.user.internal.application.DailyGoalSummary;
import com.workoutdone.rpgym.user.internal.application.GetUserHealthContextResult;
import com.workoutdone.rpgym.user.internal.application.GetUserHealthContextService;
import com.workoutdone.rpgym.user.internal.application.HealthProfileSummary;
import com.workoutdone.rpgym.user.user.domain.UserErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalUserHealthContextController.class)
class InternalUserHealthContextControllerTest {

    private static final String URL = "/api/v1/internal/users/{userId}/health-contexts";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetUserHealthContextService getUserHealthContextService;

    @Test
    @DisplayName("바디 프로필/일일 목표가 모두 등록돼 있으면 200과 함께 둘 다 채워서 반환한다")
    void getHealthContext_bothRegistered_returnsBothFilled() throws Exception {
        UUID userId = UUID.randomUUID();
        GetUserHealthContextResult result = GetUserHealthContextResult.builder()
                .healthProfile(HealthProfileSummary.builder()
                        .height(BigDecimal.valueOf(170.5))
                        .weight(BigDecimal.valueOf(65.2))
                        .build())
                .dailyGoal(DailyGoalSummary.builder()
                        .stepGoal(5000)
                        .activeMinutesGoal(60)
                        .activeCaloriesGoal(500)
                        .build())
                .longTermGoals(null)
                .build();
        given(getUserHealthContextService.getHealthContext(userId)).willReturn(result);

        mockMvc.perform(get(URL, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.healthProfile.height").value(170.5))
                .andExpect(jsonPath("$.healthProfile.weight").value(65.2))
                .andExpect(jsonPath("$.dailyGoal.stepGoal").value(5000))
                .andExpect(jsonPath("$.dailyGoal.activeMinutesGoal").value(60))
                .andExpect(jsonPath("$.dailyGoal.activeCaloriesGoal").value(500))
                .andExpect(jsonPath("$.longTermGoals").value(nullValue()));
    }

    @Test
    @DisplayName("바디 프로필이 미등록이면 healthProfile은 null로 반환한다")
    void getHealthContext_healthProfileNotRegistered_returnsNullHealthProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        GetUserHealthContextResult result = GetUserHealthContextResult.builder()
                .healthProfile(null)
                .dailyGoal(DailyGoalSummary.builder()
                        .stepGoal(5000)
                        .activeMinutesGoal(60)
                        .activeCaloriesGoal(500)
                        .build())
                .longTermGoals(null)
                .build();
        given(getUserHealthContextService.getHealthContext(userId)).willReturn(result);

        mockMvc.perform(get(URL, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.healthProfile").value(nullValue()))
                .andExpect(jsonPath("$.dailyGoal.stepGoal").value(5000));
    }

    @Test
    @DisplayName("일일 목표가 미등록이면 dailyGoal은 null로 반환한다")
    void getHealthContext_dailyGoalNotRegistered_returnsNullDailyGoal() throws Exception {
        UUID userId = UUID.randomUUID();
        GetUserHealthContextResult result = GetUserHealthContextResult.builder()
                .healthProfile(HealthProfileSummary.builder()
                        .height(BigDecimal.valueOf(170.5))
                        .weight(BigDecimal.valueOf(65.2))
                        .build())
                .dailyGoal(null)
                .longTermGoals(null)
                .build();
        given(getUserHealthContextService.getHealthContext(userId)).willReturn(result);

        mockMvc.perform(get(URL, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.healthProfile.height").value(170.5))
                .andExpect(jsonPath("$.dailyGoal").value(nullValue()));
    }

    @Test
    @DisplayName("존재하지 않거나 탈퇴한 사용자면 404 USER_NOT_FOUND를 반환한다")
    void getHealthContext_userNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        given(getUserHealthContextService.getHealthContext(any()))
                .willThrow(new BaseException(UserErrorCode.USER_NOT_FOUND));

        mockMvc.perform(get(URL, userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }
}
