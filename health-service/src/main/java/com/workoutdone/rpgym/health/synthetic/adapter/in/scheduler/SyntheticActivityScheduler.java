package com.workoutdone.rpgym.health.synthetic.adapter.in.scheduler;

import com.workoutdone.rpgym.health.activity.application.HealthActivityQueryUseCase;
import com.workoutdone.rpgym.health.activity.application.HealthActivitySyncUseCase;
import com.workoutdone.rpgym.health.activity.application.HealthActivityView;
import com.workoutdone.rpgym.health.activity.application.SyncHealthActivityCommand;
import com.workoutdone.rpgym.health.synthetic.application.SyntheticActivityGenerator;
import com.workoutdone.rpgym.health.synthetic.config.SyntheticProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Synthetic 건강 활동 수집기 (Driving Adapter).
 *
 * 외부 건강 데이터 소스를 대신해 주기적으로 동기화 유스케이스를 호출한다.
 * 저장 / 멱등 처리 / Outbox 적재는 기존 sync 유스케이스가 그대로 담당하므로
 * 이 어댑터는 "언제, 누구의, 어떤 값을" 넣을지만 결정한다.
 *
 * fixedDelay를 쓴다. 한 라운드가 끝난 뒤 다음 라운드가 돌아야
 * DB가 느릴 때 라운드가 겹쳐 쌓이지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "rpgym.synthetic", name = "enabled", havingValue = "true")
public class SyntheticActivityScheduler {

    private final SyntheticProperties properties;
    private final SyntheticActivityGenerator generator;
    private final HealthActivitySyncUseCase healthActivitySyncUseCase;
    private final HealthActivityQueryUseCase healthActivityQueryUseCase;

    @Scheduled(fixedDelayString = "${rpgym.synthetic.interval-ms:60000}")
    public void collect() {
        /*
         * measuredAt은 (user_id, measured_at) 유니크 키의 일부다.
         * 나노초까지 들어가면 사실상 매번 새 행이 되어 재동기화 경로를 검증할 수 없으므로
         * 초 단위로 끊어 한 라운드의 모든 사용자가 같은 기준 시각을 갖게 한다.
         */
        Instant measuredAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        for (UUID userId : properties.userIds()) {
            try {
                collectOne(userId, measuredAt);
            } catch (Exception e) {
                /* 한 사용자의 실패가 나머지 사용자를 막지 않는다. */
                log.warn("Synthetic 활동 생성에 실패했다. userId={}", userId, e);
            }
        }
    }

    private void collectOne(UUID userId, Instant measuredAt) {
        HealthActivityView latest = healthActivityQueryUseCase.getToday(userId);
        SyncHealthActivityCommand command = generator.next(userId, latest, measuredAt);

        healthActivitySyncUseCase.sync(command);

        log.debug("Synthetic 활동을 동기화했다. userId={} steps={} activeMinutes={} activeCalories={}",
                userId, command.steps(), command.activeMinutes(), command.activeCalories());
    }
}