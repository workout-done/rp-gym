package com.workoutdone.rpgym.game.character.application;


import com.workoutdone.rpgym.game.character.domain.XpClient;
import com.workoutdone.rpgym.game.character.domain.CharacterReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // XP 가 원본이고 characters 는 파생값 저장소다. 행이 없어도 XP 는 존재할 수 있다.
        int totalXp = xpClient.findTotalXp(userId);

        return characterReader.findByUserId(userId)
                .map(character -> CharacterView.of(character, totalXp))
                .orElseGet(() -> CharacterView.empty(userId, totalXp));
    }
}
