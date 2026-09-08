package com.workoutdone.rpgym.health.summary.adapter.out;

import com.workoutdone.rpgym.health.activity.application.DailyProgressUpdatePort;
import com.workoutdone.rpgym.health.activity.application.SyncedActivity;
import com.workoutdone.rpgym.health.summary.application.HealthSummarySyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DailyProgressUpdatePortAdapter implements DailyProgressUpdatePort {

    private final HealthSummarySyncService healthSummarySyncService;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void applySync(SyncedActivity syncedActivity) {
        healthSummarySyncService.sync(syncedActivity);
    }
}
