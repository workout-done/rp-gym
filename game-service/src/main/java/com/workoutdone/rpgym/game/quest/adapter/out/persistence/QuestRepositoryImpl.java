package com.workoutdone.rpgym.game.quest.adapter.out.persistence;

import com.workoutdone.rpgym.game.quest.domain.QuestStatus;
import com.workoutdone.rpgym.game.quest.domain.aggregate.Quest;
import com.workoutdone.rpgym.game.quest.domain.repo.QuestRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class QuestRepositoryImpl implements QuestRepository {

    private final QuestJpaRepository questJpaRepository;

    @Override
    public Optional<Quest> findById(UUID questId) {
        return questJpaRepository.findById(questId);
    }

    @Override
    public Quest save(Quest quest) {
        return questJpaRepository.save(quest);
    }

    @Override
    public Optional<Quest> findActiveByUserId(UUID userId, Instant now) {
        return questJpaRepository.findFirstByUserIdAndStatusAndExpiredAtAfterOrderByExpiredAtAsc(
                userId, QuestStatus.ACTIVE, now);
    }
}
