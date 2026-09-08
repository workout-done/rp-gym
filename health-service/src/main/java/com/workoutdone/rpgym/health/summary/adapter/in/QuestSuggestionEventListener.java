package com.workoutdone.rpgym.health.summary.adapter.in;

import com.workoutdone.rpgym.health.outbox.application.EventOutboxPort;
import com.workoutdone.rpgym.health.outbox.domain.HealthEventType;
import com.workoutdone.rpgym.health.summary.application.DeficientGoalDetectedEvent;
import com.workoutdone.rpgym.health.summary.application.QuestSuggestionAiPort;
import com.workoutdone.rpgym.health.summary.domain.MetricType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * DeficientGoalDetectedEvent를 받아 AI Quest 제안을 생성하고 Outbox에 기록한다.
 *
 * 일반 @EventListener를 사용한다 (AFTER_COMMIT 아님).
 * EventOutboxPort.append()는 도메인 저장과 같은 트랜잭션 안에서 호출해야 하므로
 * (EventOutboxPort 문서 참조), HealthSummarySyncService.sync()의 트랜잭션이
 * 아직 열려 있는 상태에서 동기로 처리한다.
 *
 * 트레이드오프: AI 호출(및 재시도)이 끝날 때까지 sync() 응답이 지연된다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuestSuggestionEventListener {

    private static final String FALLBACK_STEPS_TITLE = "1,500보 더 걷기";
    private static final int FALLBACK_STEPS_TARGET = 1500;
    private static final String FALLBACK_ACTIVE_MINUTES_TITLE = "20분 산책하기";
    private static final int FALLBACK_ACTIVE_MINUTES_TARGET = 20;
    private static final String FALLBACK_ACTIVE_CALORIES_TITLE = "100kcal 운동하기";
    private static final int FALLBACK_ACTIVE_CALORIES_TARGET = 100;

    private final QuestSuggestionAiPort aiPort;
    private final RetryTemplate retryTemplate;
    private final EventOutboxPort eventOutboxPort;

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(DeficientGoalDetectedEvent event) {
        MetricType metricType = MetricType.valueOf(event.mostDeficientMetric());
        int targetValue = resolveTargetValue(metricType);

        String title;
        try {
            title = retryTemplate.execute(context ->
                    aiPort.generateTitle(metricType, event.shortageValue())
            );
        } catch (Exception e) {
            log.warn("AI Quest 제안 생성 실패, fallback 템플릿 사용. userId={}, metric={}",
                    event.userId(), metricType, e);
            title = resolveFallbackTitle(metricType);
        }

        UUID eventId = UUID.randomUUID();
        String dedupKey = "QUEST_SUGGESTED:%s:%s".formatted(event.userId(), event.measuredAt());

        QuestSuggestedPayload payload = new QuestSuggestedPayload(
                eventId,
                event.activityDate(),
                event.measuredAt(),
                title,
                metricType.name(),
                targetValue
        );

        eventOutboxPort.append(
                eventId,
                HealthEventType.QUEST_SUGGESTED,
                event.userId(),
                event.activityId(),
                dedupKey,
                payload
        );
    }

    private int resolveTargetValue(MetricType metricType) {
        return switch (metricType) {
            case STEPS -> FALLBACK_STEPS_TARGET;
            case ACTIVE_MINUTES -> FALLBACK_ACTIVE_MINUTES_TARGET;
            case ACTIVE_CALORIES -> FALLBACK_ACTIVE_CALORIES_TARGET;
        };
    }

    private String resolveFallbackTitle(MetricType metricType) {
        return switch (metricType) {
            case STEPS -> FALLBACK_STEPS_TITLE;
            case ACTIVE_MINUTES -> FALLBACK_ACTIVE_MINUTES_TITLE;
            case ACTIVE_CALORIES -> FALLBACK_ACTIVE_CALORIES_TITLE;
        };
    }
}