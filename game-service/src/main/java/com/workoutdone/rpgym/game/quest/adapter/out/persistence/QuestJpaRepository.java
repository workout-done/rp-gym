package com.workoutdone.rpgym.game.quest.adapter.out.persistence;

import com.workoutdone.rpgym.game.quest.domain.QuestStatus;
import com.workoutdone.rpgym.game.quest.domain.aggregate.Quest;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface QuestJpaRepository extends JpaRepository<Quest, UUID> {

    Optional<Quest> findFirstByUserIdAndStatusAndExpiredAtAfterOrderByExpiredAtAsc(
            UUID userId, QuestStatus status, Instant now);

    boolean existsBySuggestionId(UUID suggestionId);
}
