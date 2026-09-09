package com.workoutdone.rpgym.game.character.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;


//캐릭터 애그리거트 루트.
@Entity
@Getter
@Table(name = "characters",
       schema = "game_service",
       uniqueConstraints = @UniqueConstraint(
               name = "uk_characters_user_id",
               columnNames = "user_id"
       ))
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "level", nullable = false)
    private int level;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** JPA 전용. 직접 호출하지 않는다. */
    protected Character(){}

    /** 외부에서 직접 new 하지 못하게 막는다. 생성은 팩토리 메서드로만. */
    private Character(UUID userId, int level){
        this.userId = userId;
        this.level = level;
    }



    /**
     * 신규 캐릭터를 만든다. 레벨은 항상 {@link LevelPolicy#INITIAL_LEVEL} 에서 시작한다.
     *
     * 조회 API 는 이 메서드를 호출하지 않는다 GET 이 데이터를 바꾸면 안 되고,
     * 동시 요청 시 중복 생성 경합이 따라오기 때문이다
     *
     * 운영 경로의 캐릭터 생성은 {@code CharacterRepository.upsertLevel()} 의
     * {@code ON CONFLICT} 가 담당한다. 단일 statement 라 경합이 없기 때문이다.
     * 이 팩토리는 재구축 배치와 테스트에서 쓴다.
     */
    public static Character create(UUID userId){
        if (userId == null){
            throw new IllegalArgumentException("userId는 필수입니다.");
        }
        return new Character(userId, LevelPolicy.INITIAL_LEVEL);
    }

    public static Character restore(UUID userId, int level){
        return new Character(userId, level);
    }

    public boolean applyXp(int totalXp){
        int recalculated = LevelPolicy.levelOf(totalXp);
        if (recalculated == this.level){
            return false;
        }
        this.level = recalculated;
        return true;
    }

    public CharacterTier tier(){
        return CharacterTier.of(this.level);
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

}
