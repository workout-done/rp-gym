package com.workoutdone.rpgym.game.character.domain;


//누적 xp를 레벨과 레벨구간 진행도를 계산하는 정책이오.
//mvp는 현재 정책으로 고정하겠소.
public class LevelPolicy {

    /** 한 레벨을 올리는 데 필요한 XP. */
    public static final int XP_PER_LEVEL = 100;

    /** 최초 레벨. characters.level 의 DEFAULT 와 같다. */
    public static final int INITIAL_LEVEL = 1;


    private LevelPolicy(){}


    //누적 xp로 레벨계산. xp 0이면 레벨 1
    public static int levelOf(int totalXp){
        validate(totalXp);
        return INITIAL_LEVEL + (totalXp / XP_PER_LEVEL);
    }
    //현재 레벨구간에서 쌓인 xp
    public static int currentLevelXp(int totalXp){
        validate(totalXp);
        return totalXp % XP_PER_LEVEL;
    }

    //다음레벨까지 필요한 총 xp
    public static int xpForNextLevel(int totalXp){
        validate(totalXp);
        return XP_PER_LEVEL;
    }

    //다음레벨까지의 진행률(%) 소수 첫째자리에서 반올림함.
    public static double progressPercent(int totalXp){
        double raw = currentLevelXp(totalXp) * 100.0 / xpForNextLevel(totalXp);
        return Math.round(raw * 10) / 10.0;
    }

    private static void validate(int totalXp){
        if (totalXp < 0){
            throw new IllegalArgumentException("누적 XP는 음수일 수 없다.: " + totalXp);
        }
    }


}
