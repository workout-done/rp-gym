package com.workoutdone.rpgym.game.character.domain;

public enum CharacterTier {
    BRONZE(1, 9),
    SILVER(10, 19),
    GOLD(20, 29),
    PLATINUM(30, 39),
    DIAMOND(40, Integer.MAX_VALUE);

    private final int minLevel;
    private final int maxLevel;

    CharacterTier(int minLevel, int maxLevel){
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
    }

    /**
     * 레벨로 티어를 계산한다.
     *
     * @param level 1 이상의 캐릭터 레벨
     * @throws IllegalArgumentException level 이 1 미만인 경우
     */
    public static CharacterTier of(int level){
        if (level < 1){
            throw new IllegalArgumentException("레벨은 1 이상이어야 합니다: " + level);
        }
        for (CharacterTier tier : values()){
            if (level >= tier.minLevel && level <= tier.maxLevel){
                return tier;
            }
        }
        return DIAMOND;
    }

    public int getMinLevel(){
        return minLevel;
    }
    public int getMaxLevel(){
        return maxLevel;
    }
}
