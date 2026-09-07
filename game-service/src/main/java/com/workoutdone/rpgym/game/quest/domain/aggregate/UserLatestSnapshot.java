package com.workoutdone.rpgym.game.quest.domain.aggregate;

import com.workoutdone.rpgym.common.entity.BaseCreatedUpdatedEntity;
import com.workoutdone.rpgym.game.quest.domain.vo.Snapshot;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Table(name = "user_latest_snapshots", schema = "game_service")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLatestSnapshot extends BaseCreatedUpdatedEntity {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @Column(name = "measured_at", nullable = false)
    private Instant measuredAt;

    @Column(name = "steps", nullable = false)
    private int steps;

    @Column(name = "active_minutes", nullable = false)
    private int activeMinutes;

    @Column(name = "active_calories", nullable = false)
    private int activeCalories;

    public static UserLatestSnapshot create(UUID userId, Snapshot snapshot) {
        UserLatestSnapshot latest = new UserLatestSnapshot();
        latest.userId = userId;
        latest.overwrite(snapshot);
        return latest;
    }

    public boolean applyIfNewer(Snapshot snapshot) {
        if (!snapshot.measuredAt().isAfter(measuredAt)) {
            return false;
        }
        overwrite(snapshot);
        return true;
    }

    public Snapshot toSnapshot() {
        return new Snapshot(activityDate, measuredAt, steps, activeMinutes, activeCalories);
    }

    private void overwrite(Snapshot snapshot) {
        this.activityDate = snapshot.activityDate();
        this.measuredAt = snapshot.measuredAt();
        this.steps = snapshot.steps();
        this.activeMinutes = snapshot.activeMinutes();
        this.activeCalories = snapshot.activeCalories();
    }
}
