package com.workoutdone.rpgym.user.healthprofile.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workoutdone.rpgym.common.exception.BaseException;
import com.workoutdone.rpgym.user.healthprofile.adapter.in.web.dto.ReqRegisterHealthProfileDto;
import com.workoutdone.rpgym.user.healthprofile.application.RegisterHealthProfileResult;
import com.workoutdone.rpgym.user.healthprofile.application.RegisterHealthProfileService;
import com.workoutdone.rpgym.user.healthprofile.domain.HealthProfileErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthProfileController.class)
class HealthProfileControllerTest {

    private static final String REGISTER_URL = "/api/v1/health-profiles/me";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterHealthProfileService registerHealthProfileService;

    private ReqRegisterHealthProfileDto validRequest() {
        return ReqRegisterHealthProfileDto.builder()
                .height(BigDecimal.valueOf(170.5))
                .weight(BigDecimal.valueOf(65.2))
                .build();
    }

    @Test
    @DisplayName("USER role이고 정상 요청이면 201과 함께 바디 프로필 정보를 반환한다")
    void registerHealthProfile_success() throws Exception {
        RegisterHealthProfileResult result = RegisterHealthProfileResult.builder()
                .id(UUID.randomUUID())
                .height(BigDecimal.valueOf(170.5))
                .weight(BigDecimal.valueOf(65.2))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        given(registerHealthProfileService.registerHealthProfile(any())).willReturn(result);

        mockMvc.perform(post(REGISTER_URL)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.height").value(170.5))
                .andExpect(jsonPath("$.weight").value(65.2));
    }

    @Test
    @DisplayName("X-User-Role이 ADMIN이면 403 FORBIDDEN을 반환한다")
    void registerHealthProfile_adminForbidden() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("X-User-Role이 USER가 아니면 403 FORBIDDEN을 반환한다")
    void registerHealthProfile_disallowedRole() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "GUEST")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("X-User-Id/X-User-Role 헤더가 둘 다 없으면 401 UNAUTHORIZED를 반환한다")
    void registerHealthProfile_noHeaders() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("X-User-Role 헤더만 없으면 401 UNAUTHORIZED를 반환한다")
    void registerHealthProfile_missingRoleHeader() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("height가 없으면 400 INVALID_INPUT을 반환한다")
    void registerHealthProfile_missingHeight() throws Exception {
        ReqRegisterHealthProfileDto request = ReqRegisterHealthProfileDto.builder()
                .weight(BigDecimal.valueOf(65.2))
                .build();

        mockMvc.perform(post(REGISTER_URL)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("height가 0 이하면 400 INVALID_INPUT을 반환한다")
    void registerHealthProfile_heightNotPositive() throws Exception {
        ReqRegisterHealthProfileDto request = ReqRegisterHealthProfileDto.builder()
                .height(BigDecimal.ZERO)
                .weight(BigDecimal.valueOf(65.2))
                .build();

        mockMvc.perform(post(REGISTER_URL)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("weight가 없으면 400 INVALID_INPUT을 반환한다")
    void registerHealthProfile_missingWeight() throws Exception {
        ReqRegisterHealthProfileDto request = ReqRegisterHealthProfileDto.builder()
                .height(BigDecimal.valueOf(170.5))
                .build();

        mockMvc.perform(post(REGISTER_URL)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("weight가 0 이하면 400 INVALID_INPUT을 반환한다")
    void registerHealthProfile_weightNotPositive() throws Exception {
        ReqRegisterHealthProfileDto request = ReqRegisterHealthProfileDto.builder()
                .height(BigDecimal.valueOf(170.5))
                .weight(BigDecimal.valueOf(-1))
                .build();

        mockMvc.perform(post(REGISTER_URL)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("이미 등록된 바디 프로필이 있으면 409 HEALTH_PROFILE_ALREADY_EXISTS를 반환한다")
    void registerHealthProfile_alreadyExists() throws Exception {
        given(registerHealthProfileService.registerHealthProfile(any()))
                .willThrow(new BaseException(HealthProfileErrorCode.HEALTH_PROFILE_ALREADY_EXISTS));

        mockMvc.perform(post(REGISTER_URL)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("HEALTH_PROFILE_ALREADY_EXISTS"));
    }
}
