package com.workoutdone.rpgym.game.quest.adapter.in.web;

import com.workoutdone.rpgym.game.quest.adapter.in.web.dto.QuestResponse;
import com.workoutdone.rpgym.game.quest.application.QuestQueryService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Quest 조회 API.
 * 인증 주체는 게이트웨이가 넣어주는 X-User-Id 헤더에서 얻는다 (CharacterController와 동일).
 */
@RestController
@RequestMapping("/api/v1/games/quests")
@RequiredArgsConstructor
public class QuestController {

    private final QuestQueryService questQueryService;

    /**
     * 내 진행 중인 Quest 하나와 진행도.
     * 활성 Quest가 없으면 204다. 404가 아닌 이유는 "없음"이 오류가 아니라 정상 상태이기 때문이다 --
     * 하루 한 개 규칙 아래에서 아직 제안을 못 받았거나 오늘 것을 이미 끝낸 유저가 여기 해당한다.
     * 캐릭터처럼 기본값을 만들어 200으로 줄 수도 없다. 없는 Quest의 목표치란 게 없다.
     */
    @GetMapping("/me/active")
    public ResponseEntity<QuestResponse> getMyActiveQuest(
            @RequestHeader("X-User-Id") UUID userId
    ) {
        return questQueryService.findActive(userId)
                .map(QuestResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
