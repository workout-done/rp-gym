package com.workoutdone.rpgym.game.quest.application;

import com.workoutdone.rpgym.game.outbox.application.OutboxRecorder;
import com.workoutdone.rpgym.game.outbox.domain.AggregateType;
import com.workoutdone.rpgym.game.outbox.domain.OutboxEventType;
import com.workoutdone.rpgym.game.quest.application.payload.QuestCreatedData;
import com.workoutdone.rpgym.game.quest.domain.Metric;
import com.workoutdone.rpgym.game.quest.domain.aggregate.Quest;
import com.workoutdone.rpgym.game.quest.domain.aggregate.UserLatestSnapshot;
import com.workoutdone.rpgym.game.quest.domain.repo.QuestRepository;
import com.workoutdone.rpgym.game.quest.domain.repo.UserLatestSnapshotRepository;
import com.workoutdone.rpgym.game.quest.domain.vo.Snapshot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestSuggestionService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final LocalTime END_OF_DAY = LocalTime.of(23, 59, 59);

    private final QuestRepository questRepository;
    private final UserLatestSnapshotRepository userLatestSnapshotRepository;
    private final RewardPolicy rewardPolicy;
    private final OutboxRecorder outboxRecorder;

    @Transactional
    public SuggestionOutcome accept(QuestSuggestionCommand command) {
        // 같은 제안이 다시 전송되는 것은 at-least-once(카프카)에서 정상이다.
        // 상태와 무관하게 여기서 걸러야 한다 .
        // ALREADY_ACTIVE if문은
        // 퀘스트가 COMPLETED/EXPIRED가 된 뒤의 재송신을 통과시키고,
        // 그러면 uk_quests_suggestion 위반이 예외로 터져 파티션이 멈춘다.
        // 따라서 멱등성 방어에 대한 방어 코드를 애플리케이션 레이어에도 작성함으로써
        // DB 유니크(2차)가 DB layer가 수행되도록 1차 방어를 여기서 해야한다.
        if (questRepository.existsBySuggestionId(command.suggestionId())) {
            return discarded(SuggestionOutcome.DUPLICATE_SUGGESTION, command);
        }

        if (questRepository.findActiveByUserId(command.userId(), Instant.now()).isPresent()) {
            return discarded(SuggestionOutcome.ALREADY_ACTIVE, command);
        }

        Optional<Metric> metric = Metric.from(command.metric());
        if (metric.isEmpty()) {
            return rejected(SuggestionOutcome.UNKNOWN_METRIC, command);
        }
        if (command.targetValue() <= 0) {
            return rejected(SuggestionOutcome.INVALID_TARGET, command);
        }

        Optional<UserLatestSnapshot> stored = userLatestSnapshotRepository.findByUserId(command.userId());
        if (stored.isEmpty()) {
            return rejected(SuggestionOutcome.SNAPSHOT_MISSING, command);
        }
        // baseLine 조달 -> 저장된 엔티티가 snapshot과 같은 5개 필드를 가짐
        // 스냅샷의 내부 함수가 3지표 중 하나를 꺼낸다.
        Snapshot baseline = stored.get().toSnapshot();
        if (!baseline.measuredAt().equals(command.basedOnMeasuredAt())) {
            return rejected(SuggestionOutcome.SNAPSHOT_MISMATCH, command);
        }
        if (!baseline.activityDate().equals(command.activityDate())) {
            return discarded(SuggestionOutcome.DATE_MISMATCH, command);
        }

        Quest quest = questRepository.save(Quest.create(
                UUID.randomUUID(),
                command.userId(),
                command.suggestionId(),
                command.title(),
                metric.get(),
                command.targetValue(),
                baseline.valueOf(metric.get()),
                baseline.measuredAt(),
                rewardPolicy.questRewardXp(),
                endOfDay(command.activityDate())
        ));

        outboxRecorder.append(
                AggregateType.QUEST,
                quest.getQuestId(),
                OutboxEventType.QUEST_CREATED,
                quest.getUserId(),
                quest.getBaselineMeasuredAt(),
                QuestCreatedData.from(quest)
        );

        log.info("quest created: questId={} userId={} suggestionId={} metric={} target={} baseline={}",
                quest.getQuestId(), quest.getUserId(), quest.getSuggestionId(),
                quest.getMetric(), quest.getTargetVal(), quest.getBaselineVal());

        return SuggestionOutcome.CREATED;
    }

    private static Instant endOfDay(LocalDate activityDate) {
        return activityDate.atTime(END_OF_DAY).atZone(KST).toInstant();
    }

    private SuggestionOutcome discarded(SuggestionOutcome outcome, QuestSuggestionCommand command) {
        log.info("quest suggestion discarded: outcome={} suggestionId={} userId={}",
                outcome, command.suggestionId(), command.userId());
        return outcome;
    }

    private SuggestionOutcome rejected(SuggestionOutcome outcome, QuestSuggestionCommand command) {
        log.error("quest suggestion rejected: outcome={} suggestionId={} userId={} metric={} target={} basedOnMeasuredAt={}",
                outcome, command.suggestionId(), command.userId(),
                command.metric(), command.targetValue(), command.basedOnMeasuredAt());
        return outcome;
    }
}
