package com.workoutdone.rpgym.game.character.domain;


import java.util.Optional;
import java.util.UUID;

public interface CharacterReader {

    Optional<Character> findByUserId(UUID userId);
}
