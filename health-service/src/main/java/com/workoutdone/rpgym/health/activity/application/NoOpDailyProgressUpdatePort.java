package com.workoutdone.rpgym.health.activity.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ⚠️ 임시 구현. Health Summary 구현이 들어오면 이 파일을 삭제한다.
 *
 * 포트만 있고 구현이 없으면 HealthActivityService 빈 생성이 실패해 서비스가 뜨지 않는다.
 * Summary 구현이 머지되기 전까지 동기화 API를 계속 쓸 수 있도록 두는 자리 표시자다.
 *
 * 실제 구현이 @Component로 등록되면 같은 타입의 빈이 둘이 되어 기동에 실패한다.
 * 조용히 무시되는 것보다 낫다 — 삭제를 잊을 수 없게 만드는 장치다.
 */
@Slf4j
@Component
public class NoOpDailyProgressUpdatePort implements DailyProgressUpdatePort {

    @Override
    public void applySync(SyncedActivity syncedActivity) {
        log.debug("Health Summary 구현 전이라 요약 갱신을 건너뛴다. activityId={}",
                syncedActivity.activityId());
    }
}