package com.workoutdone.rpgym.health.activity.domain;

import com.workoutdone.rpgym.common.entity.BaseCreatedUpdatedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

/**
 * 건강 활동 데이터 (애그리거트 루트).
 *
 * 각 행은 measuredAt 시점까지의 "당일 누적값 스냅샷"이며 증분이 아니다.
 * (userId, measuredAt) 유니크로 중복 동기화를 멱등 처리한다.
 *
 * EventOutbox와는 별개의 애그리거트다. 변경 주체(동기화 요청/발행 스케줄러)가 다르고
 * 불변식도 서로 독립적이라, 객체 참조로 묶지 않고 outbox 쪽이 activityId만 들고 참조한다.
 */
@Getter
@Entity
@Table(
        name = "health_activities",
        schema = "health_service",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_health_activities_user_measured",
                columnNames = {"user_id", "measured_at"}
        ),
        // 최신 스냅샷 및 일자별 조회
        indexes = @Index(
                name = "idx_health_activities_user_date",
                columnList = "user_id, activity_date, measured_at desc"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HealthActivity extends BaseCreatedUpdatedEntity {

    /** 일자 경계 기준 타임존 (명세: Asia/Seoul) */
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Id
    @Column(name = "activity_id", nullable = false, updatable = false)
    private UUID activityId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    /** measuredAt에서 KST 기준으로 파생된다. 외부에서 주입받지 않는다. */
    @Column(name = "activity_date", nullable = false, updatable = false)
    private LocalDate activityDate;

    /**
     * 데이터 기준 시각. 순서·중복 판정은 전부 이 값 기준이다.
     * 이벤트의 occurredAt(발행 시각)은 재전송하면 바뀌므로 판정에 쓰지 않는다.
     */
    @Column(name = "measured_at", nullable = false, updatable = false)
    private Instant measuredAt;

    @Embedded
    private ActivitySnapshot snapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 20, nullable = false)
    private ActivitySource source;

    /**
     * 낙관적 락.
     *
     * ID를 애플리케이션이 주입하는 구조라 Spring Data가 신규 여부를 판단할 근거가 필요한데,
     * version이 null이면 신규로 보므로 save()가 merge(SELECT 후 INSERT) 대신 persist()를 탄다.
     * 하루 48,000건이 들어오는 쓰기 경로라 이 SELECT 한 번이 그대로 낭비가 된다.
     *
     * 동시에 같은 (userId, measuredAt) 재동기화가 들어올 때의 lost update도 함께 막는다.
     * game-service의 Quest와 동일한 방식이다.
     *
     * ※ 절대 직접 대입하지 않는다. 0을 넣으면 null이 아니게 되어 다시 merge를 탄다.
     *   Hibernate가 persist 시점에 채운다. 반드시 박싱 타입 Long이어야 한다.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    private HealthActivity(UUID activityId,
                           UUID userId,
                           Instant measuredAt,
                           ActivitySnapshot snapshot,
                           ActivitySource source) {
        this.activityId = activityId;
        this.userId = userId;
        this.measuredAt = measuredAt;
        this.activityDate = measuredAt.atZone(KST).toLocalDate();
        this.snapshot = snapshot;
        this.source = source;
    }

    /**
     * 건강 데이터 동기화 건 하나를 생성한다.
     *
     * todo : 호출 전에 애플리케이션 계층이 확인해야 하는 것 (여기서 하지 않는다 — DB 조회가 필요하므로):
     * todo : - 같은 (userId, measuredAt) 건이 이미 있는지 → 있으면 resync()를 호출한다
     */
    public static HealthActivity sync(UUID activityId,
                                      UUID userId,
                                      Instant measuredAt,
                                      ActivitySnapshot snapshot,
                                      ActivitySource source) {
        return new HealthActivity(activityId, userId, measuredAt, snapshot, source);
    }

    /**
     * 동일한 (userId, measuredAt)으로 재동기화된 경우 스냅샷만 갱신한다.
     * 식별 정보(userId, measuredAt, activityDate)는 바뀌지 않는다.
     */
    public void resync(ActivitySnapshot snapshot, ActivitySource source) {
        this.snapshot = snapshot;
        this.source = source;
    }

    /** 값이 실제로 달라졌는지 (불필요한 UPDATE 방지용) */
    public boolean hasSameSnapshot(ActivitySnapshot other) {
        return this.snapshot.equals(other);
    }
}