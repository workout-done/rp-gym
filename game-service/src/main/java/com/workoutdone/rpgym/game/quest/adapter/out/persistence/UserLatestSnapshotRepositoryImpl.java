package com.workoutdone.rpgym.game.quest.adapter.out.persistence;

import com.workoutdone.rpgym.game.quest.domain.aggregate.UserLatestSnapshot;
import com.workoutdone.rpgym.game.quest.domain.repo.UserLatestSnapshotRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserLatestSnapshotRepositoryImpl implements UserLatestSnapshotRepository {

    private final UserLatestSnapshotJpaRepository userLatestSnapshotJpaRepository;

    @Override
    public Optional<UserLatestSnapshot> findByUserId(UUID userId) {
        return userLatestSnapshotJpaRepository.findById(userId);
    }

    @Override
    public UserLatestSnapshot save(UserLatestSnapshot snapshot) {
        return userLatestSnapshotJpaRepository.save(snapshot);
    }
}
