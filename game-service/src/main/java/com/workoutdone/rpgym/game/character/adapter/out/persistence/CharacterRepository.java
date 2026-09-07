package com.workoutdone.rpgym.game.character.adapter.out.persistence;

import com.workoutdone.rpgym.game.character.domain.Character;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


//캐릭터 저장소.
//조회 경로에서는 findByUserId만 씀.
public interface CharacterRepository extends JpaRepository<Character, UUID> {

    Optional<Character> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
