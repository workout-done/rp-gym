package com.workoutdone.rpgym.game.ranking.adapter.out.persistence;

import com.workoutdone.rpgym.game.character.adapter.out.persistence.CharacterRepository;
import com.workoutdone.rpgym.game.ranking.domain.CharacterLevelWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CharacterLevelWriterAdapter implements CharacterLevelWriter {

    private final CharacterRepository characterRepository;

    @Override
    public void upsertLevel(UUID userId, int level){
        // 새로 삽입될 때만 쓰이는 id 다. 이미 행이 있으면 ON CONFLICT 로 무시된다.
        characterRepository.upsertLevel(UUID.randomUUID(), userId, level);
    }
}
