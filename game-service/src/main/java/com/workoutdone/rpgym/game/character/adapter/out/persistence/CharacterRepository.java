package com.workoutdone.rpgym.game.character.adapter.out.persistence;

import com.workoutdone.rpgym.game.character.domain.Character;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;


//캐릭터 저장소.
//조회 경로에서는 findByUserId만 씀.
public interface CharacterRepository extends JpaRepository<Character, UUID> {

    Optional<Character> findByUserId(UUID userId);

    //캐릭터가 없으면 만들고 있으면 레벨만 갱신
    @Modifying
    @Query(value = """
            INSERT INTO game_service.characters (id, user_id, level, created_at, updated_at)
            VALUES (:id, :userId, :level, now(), now())
            ON CONFLICT (user_id)
            DO UPDATE SET level = EXCLUDED.level,
                          updated_at = now()
            """, nativeQuery = true)
    void upsertLevel(@Param("id") UUID id,
                     @Param("userId") UUID userId,
                     @Param("level") int level);
}
