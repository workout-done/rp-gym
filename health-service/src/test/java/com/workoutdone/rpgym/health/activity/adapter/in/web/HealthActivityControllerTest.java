package com.workoutdone.rpgym.health.activity.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workoutdone.rpgym.health.activity.application.*;
import com.workoutdone.rpgym.health.activity.domain.ActivitySource;
import com.workoutdone.rpgym.health.config.CommonExceptionHandlerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthActivityController.class)
@Import(CommonExceptionHandlerConfig.class)
class HealthActivityControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    HealthActivitySyncUseCase healthActivitySyncUseCase;
    @MockitoBean
    HealthActivityQueryUseCase healthActivityQueryUseCase;

    private final UUID userId = UUID.randomUUID();

    private HealthActivityView view() {
        return new HealthActivityView(
                UUID.randomUUID(), userId, LocalDate.of(2026, 8, 28),
                Instant.parse("2026-08-28T01:30:00Z"),
                3100, 31, 155, ActivitySource.SYNTHETIC);
    }

    private String body(Object steps) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "measuredAt", "2026-08-28T01:30:00Z",
                "steps", steps,
                "activeMinutes", 31,
                "activeCalories", 155,
                "source", "SYNTHETIC"));
    }

    @Test
    @DisplayName("POST /sync — 신규 저장이면 201")
    void sync_신규() throws Exception {
        given(healthActivitySyncUseCase.sync(any()))
                .willReturn(new HealthActivitySyncResult(view(), true));

        mockMvc.perform(post("/api/v1/health-activities/sync")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(3100)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.steps").value(3100))
                .andExpect(jsonPath("$.source").value("SYNTHETIC"));
    }

    @Test
    @DisplayName("POST /sync — 이미 있는 시점이면 200")
    void sync_기존() throws Exception {
        given(healthActivitySyncUseCase.sync(any()))
                .willReturn(new HealthActivitySyncResult(view(), false));

        mockMvc.perform(post("/api/v1/health-activities/sync")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(3100)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /sync — 음수 지표는 400 INVALID_INPUT")
    void sync_음수() throws Exception {
        mockMvc.perform(post("/api/v1/health-activities/sync")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(-1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.fields[0].field").value("steps"));
    }

    @Test
    @DisplayName("X-User-Id 헤더가 없으면 401 UNAUTHORIZED")
    void 헤더_누락() throws Exception {
        mockMvc.perform(get("/api/v1/health-activities/today"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("GET /today — 이력이 없어도 200 + 0값")
    void today_이력없음() throws Exception {
        given(healthActivityQueryUseCase.getToday(any()))
                .willReturn(HealthActivityView.empty(userId, LocalDate.of(2026, 8, 28)));

        mockMvc.perform(get("/api/v1/health-activities/today")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.steps").value(0))
                .andExpect(jsonPath("$.activityId").doesNotExist());
    }
}