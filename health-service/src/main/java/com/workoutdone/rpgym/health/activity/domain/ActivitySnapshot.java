package com.workoutdone.rpgym.health.activity.domain;

import com.workoutdone.rpgym.common.exception.BaseException;
import com.workoutdone.rpgym.health.activity.exception.ActivityErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 특정 시점까지의 당일 누적 활동량 스냅샷 (값 객체).
 *
 * 세 지표는 항상 한 세트로 움직이며 HealthActivitySynced 이벤트의 data.cumulative와 1:1 대응한다.
 * (steps / active_minutes / active_calories 컬럼으로 그대로 펼쳐지고 조인은 생기지 않는다)
 */
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivitySnapshot {

    @Column(name = "steps", nullable = false)
    private int steps;

    @Column(name = "active_minutes", nullable = false)
    private int activeMinutes;

    @Column(name = "active_calories", nullable = false)
    private int activeCalories;

    private ActivitySnapshot(int steps, int activeMinutes, int activeCalories) {
        this.steps = steps;
        this.activeMinutes = activeMinutes;
        this.activeCalories = activeCalories;
    }

    public static ActivitySnapshot of(int steps, int activeMinutes, int activeCalories) {
        // 클라이언트(동기화 요청)가 만들 수 있는 오류이므로 400으로 응답한다.
        if (steps < 0 || activeMinutes < 0 || activeCalories < 0) {
            throw new BaseException(ActivityErrorCode.INVALID_ACTIVITY_VALUE);
        }
        return new ActivitySnapshot(steps, activeMinutes, activeCalories);
    }

    public static ActivitySnapshot zero() {
        return new ActivitySnapshot(0, 0, 0);
    }

    /**
     * 누적값은 시간이 지날수록 커져야 한다.
     * 이전 스냅샷보다 작으면 자정 리셋이거나 데이터 정정이다.
     */
    public boolean isNotLessThan(ActivitySnapshot other) {
        return this.steps >= other.steps
                && this.activeMinutes >= other.activeMinutes
                && this.activeCalories >= other.activeCalories;
    }
}