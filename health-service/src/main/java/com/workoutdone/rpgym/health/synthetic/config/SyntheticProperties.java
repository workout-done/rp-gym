package com.workoutdone.rpgym.health.synthetic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;
import java.util.UUID;

/**
 * Synthetic 건강 활동 수집기 설정.
 *
 * MVP에는 실제 건강 데이터 소스가 없어 이 수집기가 그 자리를 대신한다.
 * 추후 Samsung Health 연동이 들어오면 수집 채널만 교체되고
 * 저장·분석·이벤트 발행 흐름은 그대로 유지된다. (기획서 20)
 *
 * 기본값을 false로 두는 이유는 팀원 로컬과 CI에서 원치 않는 데이터가 쌓이는 것을 막기 위해서다.
 * 이 수집기는 요청이 없어도 스스로 도는 스케줄러라, 켜는 쪽을 명시하게 만든다.
 *
 * 대상 사용자를 설정으로 받는 것은 외부 데이터 소스의 모양을 따른 것이다.
 * 실제 연동에서는 앱이 자기 사용자의 데이터만 보내므로 전체 사용자 목록을 조회하는 단계가 없다.
 * 목록은 로컬 계정마다 다르므로 커밋하지 않고 .env 또는 실행 인자로 주입한다.
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