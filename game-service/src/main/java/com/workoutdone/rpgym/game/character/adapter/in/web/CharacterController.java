package com.workoutdone.rpgym.game.character.adapter.in.web;


import com.workoutdone.rpgym.game.character.application.CharacterQueryUseCase;
import com.workoutdone.rpgym.game.character.adapter.in.web.dto.CharacterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 캐릭터 조회 API
 * 인증 주체는 게이트웨이가 넣어주는 X-User-Id 헤더에서 얻는다.
 */
@RestController
@RequestMapping("/api/v1/games/characters")
@RequiredArgsConstructor
public class CharacterController {

    private final CharacterQueryUseCase characterQueryUseCase;

    // 내 캐릭터 조회. 캐릭터 행이 없어도 404가 아니라 기본값 200으로 기본 브론즈 레벨1 나오도록 구현.
    @GetMapping("/me")
    public ResponseEntity<CharacterResponse> getMyCharacter(
            @RequestHeader("X-User-Id")UUID userId
            ){
        CharacterResponse response = CharacterResponse.from(characterQueryUseCase.getCharacter(userId));
        return ResponseEntity.ok(response);
    }

    //특정 사용자 캐릭터 조회. 랭킹목록에서 상세로 넘어갈때 사용할것임.
    @GetMapping("/{userId}")
    public ResponseEntity<CharacterResponse> getCharacter(
            @PathVariable UUID userId
    ){
        CharacterResponse response = CharacterResponse.from(characterQueryUseCase.getCharacter(userId));
        return ResponseEntity.ok(response);
    }
}
