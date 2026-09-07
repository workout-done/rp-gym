package com.workoutdone.rpgym.game.quest.application;

import com.workoutdone.rpgym.game.outbox.application.OutboxRecorder;
import com.workoutdone.rpgym.game.outbox.domain.AggregateType;
import com.workoutdone.rpgym.game.outbox.domain.OutboxEventType;
import com.workoutdone.rpgym.game.quest.application.payload.QuestCompletedData;
import com.workoutdone.rpgym.game.quest.domain.aggregate.Quest;
import com.workoutdone.rpgym.game.quest.domain.aggregate.UserLatestSnapshot;
import com.workoutdone.rpgym.game.quest.domain.repo.QuestRepository;
import com.workoutdone.rpgym.game.quest.domain.repo.UserLatestSnapshotRepository;
import com.workoutdone.rpgym.game.quest.domain.vo.ApplyResult;
import com.workoutdone.rpgym.game.quest.domain.vo.Snapshot;
import com.workoutdone.rpgym.game.xp.application.XpGrantService;
import com.workoutdone.rpgym.game.xp.domain.SourceType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestProgressService {

    private final QuestRepository questRepository;
    private final UserLatestSnapshotRepository userLatestSnapshotRepository;
    private final XpGrantService xpGrantService;
    private final OutboxRecorder outboxRecorder;

    @Transactional
    public Optional<ApplyResult> apply(UUID userId, Snapshot snapshot) {
        storeLatest(userId, snapshot);

        Optional<Quest> active = questRepository.findActiveByUserId(userId, snapshot.measuredAt());
        if (active.isEmpty()) {
            return Optional.empty();
        }

        Quest quest = active.get();
        ApplyResult result = quest.applySnapshot(snapshot);
        questRepository.save(quest);

        if (result instanceof ApplyResult.Completed completed) {
            xpGrantService.grant(
                    userId, SourceType.QUEST, quest.getQuestId(), quest.getRewardXp(), snapshot.measuredAt());

            outboxRecorder.append(
                    AggregateType.QUEST,
                    quest.getQuestId(),
                    OutboxEventType.QUEST_COMPLETED,
                    userId,
                    snapshot.measuredAt(),
                    QuestCompletedData.from(quest, completed.achievedDelta(), snapshot.measuredAt())
            );

            log.info("quest completed: questId={} userId={} achievedDelta={} rewardXp={} measuredAt={}",
                    quest.getQuestId(), userId, completed.achievedDelta(),
                    quest.getRewardXp(), snapshot.measuredAt());
        } else {
            log.debug("snapshot applied: questId={} userId={} measuredAt={} result={}",
                    quest.getQuestId(), userId, snapshot.measuredAt(), result);
        }

        return Optional.of(result);
    }

    private void storeLatest(UUID userId, Snapshot snapshot) {
        Optional<UserLatestSnapshot> stored = userLatestSnapshotRepository.findByUserId(userId);

        if (stored.isEmpty()) {
            userLatestSnapshotRepository.save(UserLatestSnapshot.create(userId, snapshot));
            return;
        }

        UserLatestSnapshot latest = stored.get();
        if (latest.applyIfNewer(snapshot)) {
            userLatestSnapshotRepository.save(latest);
        }
    }
}
