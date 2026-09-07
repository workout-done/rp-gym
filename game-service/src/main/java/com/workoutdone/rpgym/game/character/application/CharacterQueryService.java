package com.workoutdone.rpgym.game.character.application;


import com.workoutdone.rpgym.game.character.domain.XpClient;
import com.workoutdone.rpgym.game.character.domain.CharacterReader;
import com.workoutdone.rpgym.game.character.domain.Character;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CharacterQueryService implements CharacterQueryUseCase {

    private final CharacterReader characterReader;
    private final XpClient xpClient;

    @Override
    public CharacterView getCharacter(UUID userId){
        if (userId == null){
            throw new IllegalArgumentException("userId는 필수입니다.");
        }
        Optional<Character> character = characterReader.findByUserId(userId);
        if (character.isEmpty()){
            return CharacterView.empty(userId);
        }

        int totalXp = xpClient.findTotalXp(userId);
        return CharacterView.of(character.get(), totalXp);
    }
}
