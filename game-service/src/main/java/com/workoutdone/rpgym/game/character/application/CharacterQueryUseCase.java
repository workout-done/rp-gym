package com.workoutdone.rpgym.game.character.application;


import java.util.UUID;

public interface CharacterQueryUseCase {

    CharacterView getCharacter(UUID userId);
}
