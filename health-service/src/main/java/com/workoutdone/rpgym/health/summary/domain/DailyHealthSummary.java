package com.workoutdone.rpgym.health.summary.domain;

import com.workoutdone.rpgym.common.entity.BaseCreatedUpdatedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "daily_health_summaries",
        schema = "health_service",
        uniqueConstraints = @UniqueConstraint(
                name = "ux_daily_health_summaries_user_date",
                columnNames = {"user_id", "activity_date"}
        )
)
public class DailyHealthSummary extends BaseCreatedUpdatedEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "summary_id", updatable = false, nullable = false)
    private UUID summaryId;

    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    @Column(name = "activity_date", updatable = false, nullable = false)
    private LocalDate activityDate;

    @Column(name = "total_steps", nullable = false)
    private Integer totalSteps = 0;

    @Column(name = "total_active_minutes", nullable = false)
    private Integer totalActiveMinutes = 0;

    @Column(name = "total_active_calories", nullable = false)
    private Integer totalActiveCalories = 0;

    @Column(name = "is_all_goals_achieved", nullable = false)
    private boolean allGoalsAchieved = false;

    @Column(name = "achieved_at")
    private LocalDateTime achievedAt;

    @Column(name = "last_synced_at", nullable = false)
    private Instant lastSyncedAt;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    private DailyHealthSummary(UUID userId, LocalDate activityDate, LocalDateTime now) {
        this.userId = userId;
        this.activityDate = activityDate;
        this.calculatedAt = now;
    }

    public static DailyHealthSummary createFor(UUID userId, LocalDate activityDate, LocalDateTime now) {
        return new DailyHealthSummary(userId, activityDate, now);
    }

    public void applySync(int steps, int activeMinutes, int activeCalories, Instant measuredAt, LocalDateTime calculatedAt) {
        this.totalSteps = steps;
        this.totalActiveMinutes = activeMinutes;
        this.totalActiveCalories = activeCalories;
        this.lastSyncedAt = measuredAt;
        this.calculatedAt = calculatedAt;
    }

    public boolean markAllGoalsAchieved(LocalDateTime now) {
        if (this.achievedAt != null) {
            return false;
        }
        this.allGoalsAchieved = true;
        this.achievedAt = now;
        return true;
    }
}