package com.workoutdone.rpgym.game.quest.application;

import org.springframework.stereotype.Component;

@Component
public class RewardPolicy {

    private static final int QUEST_REWARD_XP = 5;

    public int questRewardXp() {
        return QUEST_REWARD_XP;
    }
}
