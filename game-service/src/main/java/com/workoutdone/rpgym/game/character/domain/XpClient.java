package com.workoutdone.rpgym.game.character.domain;

import java.util.UUID;

public interface XpClient {
    int findTotalXp(UUID userId);
}
