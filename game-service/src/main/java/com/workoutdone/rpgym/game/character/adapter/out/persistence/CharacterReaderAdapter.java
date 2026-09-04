package com.workoutdone.rpgym.game.character.adapter.out.persistence;

import com.workoutdone.rpgym.game.character.domain.CharacterReader;
import com.workoutdone.rpgym.game.character.domain.Character;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CharacterReaderAdapter implements CharacterReader {

    private final CharacterRepository characterRepository;

    @Override
    public Optional<Character> findByUserId(UUID userId){
        return characterRepository.findByUserId(userId);
    }
}
