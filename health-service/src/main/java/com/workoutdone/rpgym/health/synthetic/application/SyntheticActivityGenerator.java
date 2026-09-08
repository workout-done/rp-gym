package com.workoutdone.rpgym.health.synthetic.application;

import com.workoutdone.rpgym.health.activity.application.HealthActivityView;
import com.workoutdone.rpgym.health.activity.application.SyncHealthActivityCommand;
import com.workoutdone.rpgym.health.activity.domain.ActivitySource;
import com.workoutdone.rpgym.health.synthetic.config.SyntheticProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 다음 시점의 누적 활동값을 만든다.
 *
 * 직전 누적값에 증가분을 더하는 방식이라 값이 절대 줄어들지 않는다.
 * 누적값이 역전되면 Game Service에서 (누적값 - baseline)이 음수가 되어
 * Quest 진행도 계산이 깨지므로 이 불변식이 중요하다.
 *
 * 직전 값을 메모리에 들고 있지 않고 조회 결과에서 받는 이유는 두 가지다.
 * 애플리케이션을 재시작해도 이어서 누적되고, 자정이 지나면
 * 오늘 이력이 없어 0값이 넘어오므로 날짜 리셋이 자연스럽게 처리된다.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "rpgym.synthetic", name = "enabled", havingValue = "true")
public class SyntheticActivityGenerator {

    private final SyntheticProperties properties;

    public SyncHealthActivityCommand next(UUID userId, HealthActivityView latest, Instant measuredAt) {
        return new SyncHealthActivityCommand(
                userId,
                measuredAt,
                latest.steps() + randomIncrement(properties.maxStepsPerTick()),
                latest.activeMinutes() + randomIncrement(properties.maxActiveMinutesPerTick()),
                latest.activeCalories() + randomIncrement(properties.maxActiveCaloriesPerTick()),
                ActivitySource.SYNTHETIC
        );
    }

    /* 0 이상 max 이하. 0이 나올 수 있어야 활동이 없는 구간도 표현된다. */
    private int randomIncrement(int max) {
        if (max <= 0) {
            return 0;
        }
        return ThreadLocalRandom.current().nextInt(max + 1);
    }
}