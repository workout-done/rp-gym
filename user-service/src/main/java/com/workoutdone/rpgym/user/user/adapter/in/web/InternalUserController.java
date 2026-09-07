package com.workoutdone.rpgym.user.user.adapter.in.web;

import com.workoutdone.rpgym.user.user.adapter.in.web.dto.ResInternalUserInfoDto;
import com.workoutdone.rpgym.user.user.application.GetInternalUserInfoResult;
import com.workoutdone.rpgym.user.user.application.GetInternalUserInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

// 서비스 간(Health/Game -> User) Feign 호출 전용.
// 게이트웨이가 /api/v1/internal/** 외부 인입을 차단하므로 여기서는 별도 인증/인가(@RequireRole 등)를 걸지 않는다.
@RestController
@RequestMapping("/api/v1/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final GetInternalUserInfoService getInternalUserInfoService;

    @GetMapping("/{userId}")
    public ResponseEntity<ResInternalUserInfoDto> getUserInfo(@PathVariable UUID userId) {
        GetInternalUserInfoResult result = getInternalUserInfoService.getUserInfo(userId);

        return ResponseEntity.ok(ResInternalUserInfoDto.from(result));
    }
}
