package com.workoutdone.rpgym.health.activity.adapter.in.web;

import com.workoutdone.rpgym.health.activity.adapter.in.web.dto.HealthActivityResponse;
import com.workoutdone.rpgym.health.activity.adapter.in.web.dto.HealthActivitySyncRequest;
import com.workoutdone.rpgym.health.activity.application.HealthActivityQueryUseCase;
import com.workoutdone.rpgym.health.activity.application.HealthActivitySyncResult;
import com.workoutdone.rpgym.health.activity.application.HealthActivitySyncUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 건강 활동 API.
 * 인증 주체는 게이트웨이가 넣어주는 X-User-Id 헤더에서 얻는다.
 */
@RestController
@RequestMapping("/api/v1/health-activities")
@RequiredArgsConstructor
public class HealthActivityController {

    private final HealthActivitySyncUseCase healthActivitySyncUseCase;
    private final HealthActivityQueryUseCase healthActivityQueryUseCase;

    /** 건강 활동 동기화. 신규 저장이면 201, 이미 있는 시점이면 200. */
    @PostMapping("/sync")
    public ResponseEntity<HealthActivityResponse> sync(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody HealthActivitySyncRequest request
    ) {
        HealthActivitySyncResult result =
                healthActivitySyncUseCase.sync(request.toCommand(userId));

        return ResponseEntity
                .status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(HealthActivityResponse.from(result.view()));
    }

    /** 오늘의 건강 활동 조회. 이력이 없어도 404가 아니라 0값 200으로 응답한다. */
    @GetMapping("/today")
    public ResponseEntity<HealthActivityResponse> getToday(
            @RequestHeader("X-User-Id") UUID userId
    ) {
        return ResponseEntity.ok(
                HealthActivityResponse.from(healthActivityQueryUseCase.getToday(userId)));
    }
}