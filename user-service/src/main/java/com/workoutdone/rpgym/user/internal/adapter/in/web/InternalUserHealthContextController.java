package com.workoutdone.rpgym.user.internal.adapter.in.web;

import com.workoutdone.rpgym.user.internal.adapter.in.web.dto.ResUserHealthContextDto;
import com.workoutdone.rpgym.user.internal.application.GetUserHealthContextResult;
import com.workoutdone.rpgym.user.internal.application.GetUserHealthContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

// 서비스 간(Health Service -> User) Feign 호출 전용.
// 게이트웨이가 /api/v1/internal/** 외부 인입을 차단하므로 여기서는 별도 인증/인가(@RequireRole 등)를 걸지 않는다.
@RestController
@RequestMapping("/api/v1/internal/users")
@RequiredArgsConstructor
public class InternalUserHealthContextController {

    private final GetUserHealthContextService getUserHealthContextService;

    @GetMapping("/{userId}/health-contexts")
    public ResponseEntity<ResUserHealthContextDto> getHealthContext(@PathVariable UUID userId) {
        GetUserHealthContextResult result = getUserHealthContextService.getHealthContext(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResUserHealthContextDto.from(result));
    }
}
