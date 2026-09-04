package com.workoutdone.rpgym.health.activity.application;

import com.workoutdone.rpgym.health.activity.domain.ActivitySource;

import java.time.Instant;
import java.util.UUID;

/** web dto가 application 계층으로 새어 들어오지 않도록 하는 입력 모델
 * application 패키지 안의 어떤 파일도 adapter.in.web을 import하지 않는다.
 * web DTO는 컨트롤러에서 Command로 번역되고 거기서 멈춘다.
 */
public record SyncHealthActivityCommand(
        UUID userId,
        Instant measuredAt,
        int steps,
        int activeMinutes,
        int activeCalories,
        ActivitySource source
) {
}