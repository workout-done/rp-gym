package com.workoutdone.rpgym.game.quest.application;

import com.workoutdone.rpgym.game.outbox.application.OutboxRecorder;
import com.workoutdone.rpgym.game.outbox.domain.AggregateType;
import com.workoutdone.rpgym.game.outbox.domain.OutboxEventType;
import com.workoutdone.rpgym.game.quest.domain.Metric;
import com.workoutdone.rpgym.game.quest.domain.QuestStatus;
import com.workoutdone.rpgym.game.quest.domain.aggregate.Quest;
import com.workoutdone.rpgym.game.quest.domain.aggregate.UserLatestSnapshot;
import com.workoutdone.rpgym.game.quest.domain.repo.QuestRepository;
import com.workoutdone.rpgym.game.quest.domain.repo.UserLatestSnapshotRepository;
import com.workoutdone.rpgym.game.quest.domain.vo.Snapshot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QuestSuggestionServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final LocalDate DATE = LocalDate.of(2026, 8, 28);
    private static final Instant BASED_ON = Instant.parse("2026-08-28T01:30:00Z");

    private QuestRepository questRepository;
    private UserLatestSnapshotRepository snapshotRepository;
    private OutboxRecorder outboxRecorder;
    private QuestSuggestionService service;

    @BeforeEach
    void setUp() {
        questRepository = mock(QuestRepository.class);
        snapshotRepository = mock(UserLatestSnapshotRepository.class);
        outboxRecorder = mock(OutboxRecorder.class);
        service = new QuestSuggestionService(
                questRepository, snapshotRepository, new RewardPolicy(), outboxRecorder);

        when(questRepository.findActiveByUserId(any(), any())).thenReturn(Optional.empty());
        when(questRepository.save(any())).thenAnswer(call -> call.getArgument(0));
        when(snapshotRepository.findByUserId(USER_ID)).thenReturn(Optional.of(storedSnapshot()));
    }

    private UserLatestSnapshot storedSnapshot() {
        return UserLatestSnapshot.create(USER_ID, new Snapshot(DATE, BASED_ON, 3000, 31, 155));
    }

    private QuestSuggestionCommand command(String metric, int targetValue) {
        return new QuestSuggestionCommand(
                USER_ID, UUID.randomUUID(), DATE, BASED_ON, "20분 산책하기", metric, targetValue);
    }

    private QuestSuggestionCommand validCommand() {
        return command("ACTIVE_MINUTES", 20);
    }

    @Test
    @DisplayName("baseline은 저장된 스냅샷의 metric 값으로 고정된다")
    void baseline은_저장된_스냅샷에서_온다() {
        ArgumentCaptor<Quest> saved = ArgumentCaptor.forClass(Quest.class);

        assertEquals(SuggestionOutcome.CREATED, service.accept(validCommand()));

        verify(questRepository).save(saved.capture());
        Quest quest = saved.getValue();
        assertEquals(Metric.ACTIVE_MINUTES, quest.getMetric());
        assertEquals(31, quest.getBaselineVal());
        assertEquals(BASED_ON, quest.getBaselineMeasuredAt());
        assertEquals(20, quest.getTargetVal());
        assertEquals(5, quest.getRewardXp());
        assertEquals(QuestStatus.ACTIVE, quest.getStatus());
    }

    @Test
    @DisplayName("만료 시각은 activityDate의 KST 23:59:59다")
    void 만료는_당일_KST_자정_직전이다() {
        ArgumentCaptor<Quest> saved = ArgumentCaptor.forClass(Quest.class);

        service.accept(validCommand());

        verify(questRepository).save(saved.capture());
        assertEquals(Instant.parse("2026-08-28T14:59:59Z"), saved.getValue().getExpiredAt());
    }

    @Test
    @DisplayName("생성되면 QuestCreated가 Outbox에 적재된다")
    void 생성되면_Outbox에_적재된다() {
        service.accept(validCommand());

        verify(outboxRecorder).append(
                eq(AggregateType.QUEST), any(), eq(OutboxEventType.QuestCreated),
                eq(USER_ID), eq(BASED_ON), any());
    }

    @Test
    @DisplayName("이미 활성 Quest가 있으면 폐기한다")
    void 이미_활성_Quest가_있으면_폐기한다() {
        when(questRepository.findActiveByUserId(any(), any()))
                .thenReturn(Optional.of(mock(Quest.class)));

        assertEquals(SuggestionOutcome.ALREADY_ACTIVE, service.accept(validCommand()));
        verify(questRepository, never()).save(any());
        verify(outboxRecorder, never()).append(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("metric이 3종 밖이면 폐기한다")
    void metric이_3종_밖이면_폐기한다() {
        assertEquals(SuggestionOutcome.UNKNOWN_METRIC, service.accept(command("HEART_RATE", 20)));
        verify(questRepository, never()).save(any());
    }

    @Test
    @DisplayName("targetValue가 0 이하면 폐기한다 — 즉시 완료로 공짜 XP가 나간다")
    void targetValue가_0_이하면_폐기한다() {
        assertEquals(SuggestionOutcome.INVALID_TARGET, service.accept(command("STEPS", 0)));
        assertEquals(SuggestionOutcome.INVALID_TARGET, service.accept(command("STEPS", -1)));
        verify(questRepository, never()).save(any());
    }

    @Test
    @DisplayName("저장된 스냅샷이 없으면 폐기한다")
    void 스냅샷이_없으면_폐기한다() {
        when(snapshotRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertEquals(SuggestionOutcome.SNAPSHOT_MISSING, service.accept(validCommand()));
        verify(questRepository, never()).save(any());
    }

    @Test
    @DisplayName("basedOnMeasuredAt이 저장된 measuredAt과 다르면 폐기한다 — 짝 이벤트 대조 실패")
    void measuredAt이_어긋나면_폐기한다() {
        QuestSuggestionCommand mismatched = new QuestSuggestionCommand(
                USER_ID, UUID.randomUUID(), DATE,
                Instant.parse("2026-08-28T02:00:00Z"), "20분 산책하기", "ACTIVE_MINUTES", 20);

        assertEquals(SuggestionOutcome.SNAPSHOT_MISMATCH, service.accept(mismatched));
        verify(questRepository, never()).save(any());
    }

    @Test
    @DisplayName("activityDate가 어긋나면 폐기한다 — 자정을 넘긴 제안")
    void activityDate가_어긋나면_폐기한다() {
        QuestSuggestionCommand nextDay = new QuestSuggestionCommand(
                USER_ID, UUID.randomUUID(), LocalDate.of(2026, 8, 29),
                BASED_ON, "20분 산책하기", "ACTIVE_MINUTES", 20);

        assertEquals(SuggestionOutcome.DATE_MISMATCH, service.accept(nextDay));
        verify(questRepository, never()).save(any());
    }
}
