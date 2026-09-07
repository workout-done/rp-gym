package com.workoutdone.rpgym.game.ranking.domain;

public final class RankingScore {


    /** level 을 앞자리로 밀어 올리는 배수. total_xp < 10억 을 전제한다. */
    public static final long LEVEL_MULTIPLIER = 1_000_000_000L;

    private RankingScore(){}


    //level 12, totalXp 2450 → 12_000_002_450

    public static double encode(int level, int totalXp){
        if (level < 1){
            throw new IllegalArgumentException("레벨은 1이상이어야 합니다: " + level);
        }
        if (totalXp < 0){
            throw new IllegalArgumentException("누적 xp는 음수일 수 없습니다: " + totalXp);
        }
        //상한을 넘으면 배수를 키우고 재계산 배치를 돌려야 함.
        if (totalXp >= LEVEL_MULTIPLIER){
            throw new IllegalArgumentException(
                    "누적 xp가 배수 상한을 넘었습니다. 배수를 키우고 재계산 배치가 필요합니다: " + totalXp
            );
        }
        return (double) (level * LEVEL_MULTIPLIER + totalXp);
    }

    //score 앞자리에서 레벨을 떼어냄
    public static int decodeLevel(double score){
        return (int) ((long) score / LEVEL_MULTIPLIER);
    }

    //score 뒷자리에서 누적 XP 를 떼어냄
    public static int decodeXp(double score){
        return (int) ((long) score % LEVEL_MULTIPLIER);
    }
}
