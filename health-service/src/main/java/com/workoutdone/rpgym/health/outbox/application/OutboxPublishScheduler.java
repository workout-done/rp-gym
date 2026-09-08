package com.workoutdone.rpgym.health.outbox.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * PENDING 이벤트 폴링 스케줄러.
 *
 * fixedDelay를 쓴다. 이전 라운드가 끝난 뒤에 다음 폴링이 돌아야
 * 브로커가 느릴 때 라운드가 겹쳐 쌓이지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublishScheduler {

    private final OutboxRelay outboxRelay;

    @Scheduled(fixedDelayString = "${rpgym.outbox.poll-interval:1000}")
    public void publishPendingEvents() {
        try {
            int publishedCount = outboxRelay.relayOnce();

            if (publishedCount > 0) {
                log.debug("Outbox 이벤트를 발행했다. count={}", publishedCount);
            }
        } catch (Exception e) {
            /*
             * 스케줄러 메서드에서 예외가 새어 나가면 해당 작업이 중단되어
             * 이후 폴링이 전부 멈춘다. 여기서 반드시 흡수한다.
             */
            log.error("Outbox 폴링 중 예기치 못한 오류가 발생했다.", e);
        }
    }
}