package com.workoutdone.rpgym.game.domain.vo;

import com.workoutdone.rpgym.game.domain.entity.quest.Metric;

import java.time.Instant;
import java.time.LocalDate;

/**
 * {HealthActivitySynced}가 실어오는 "그 시점까지의 오늘 누적값" 스냅샷.
 *
 * 이건 Kafka DTO가 아니라 도메인 값 객체다. 컨슈머(어댑터)가 이벤트를 이 타입으로 변환해서
 * 도메인에 넘긴다. 도메인이 Jackson/Kafka 클래스를 직접 받으면 인프라를 알게 되고,
 * 판정 로직을 컨테이너 없이 테스트할 수 없게 된다.
 *
 * 증분이 아니라 누적값이다.(중요) 델타를 실어 보내면 이벤트 유실 시 영구 복구가 불가능하지만,
 * 누적값이면 다음 스냅샷이 자동으로 메운다. 이 결정이 무너지면 파트 전체 설계가 무너진다.
 *
 * @param activityDate 활동 날짜(Asia/Seoul 기준). 자정 누적값 리셋을 순서 역전과 구분하는 근거
 * @param measuredAt   데이터 기준 시각. 순서·중복·만료 판정은 전부 이 값 기준이다.
 *                     이벤트의 {occurredAt}(발행 시각)은 재전송하면 바뀌므로 판정에 쓰면 안 된다.
 */
public record Snapshot(
        LocalDate activityDate,
        Instant measuredAt,
        int steps,
        int activeMinutes,
        int activeCalories
) {

    /**
     * Quest가 보는 지표 하나의 누적값을 꺼낸다.
     *
     * Quest는 동시에 하나의 metric만 보므로, 3필드를 다 받아도 판정에 쓰는 건 한 칸이다.
     */
    public int valueOf(Metric metric) {
        return switch (metric) {
            case STEPS -> steps;
            case ACTIVE_MINUTES -> activeMinutes;
            case ACTIVE_CALORIES -> activeCalories;
        };
    }
}
