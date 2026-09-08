package com.workoutdone.rpgym.health.synthetic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;
import java.util.UUID;

/**
 * Synthetic 건강 활동 수집기 설정.
 *
 * MVP에서는 실제 건강 데이터 소스가 없으므로 서비스가 스스로 데이터를 만들어 넣는다.
 * 기본값을 false로 두는 이유는 팀원 로컬과 CI에서 원치 않는 데이터가 쌓이는 것을 막기 위해서다.
 *
 * 대상 사용자는 user-service를 호출하지 않고 설정으로 받는다.
 * 서비스 간 동기 의존이 생기면 user-service가 내려갔을 때
 * 수집 스케줄러가 계속 실패 로그만 남기게 된다. (MVP 한정, 추후 연동으로 대체)
 */
@ConfigurationProperties(prefix = "rpgym.synthetic")
public record SyntheticProperties(

        // 수집기 사용 여부 (기본 비활성)
        @DefaultValue("false") boolean enabled,

        // 데이터를 생성할 대상 사용자
        List<UUID> userIds,

        // 한 번의 수집에서 더할 수 있는 최대 증가분
        @DefaultValue("300") int maxStepsPerTick,
        @DefaultValue("2") int maxActiveMinutesPerTick,
        @DefaultValue("20") int maxActiveCaloriesPerTick
) {
    /* 설정에서 user-ids를 생략하면 null로 바인딩되므로 빈 목록으로 정규화한다. */
    public SyntheticProperties {
        userIds = (userIds == null) ? List.of() : List.copyOf(userIds);
    }
}