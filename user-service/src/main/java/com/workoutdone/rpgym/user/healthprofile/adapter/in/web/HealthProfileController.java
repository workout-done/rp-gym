package com.workoutdone.rpgym.user.healthprofile.adapter.in.web;

import com.workoutdone.rpgym.common.constant.HeaderConstants;
import com.workoutdone.rpgym.common.security.RequireRole;
import com.workoutdone.rpgym.common.security.UserRole;
import com.workoutdone.rpgym.user.healthprofile.adapter.in.web.dto.ReqRegisterHealthProfileDto;
import com.workoutdone.rpgym.user.healthprofile.adapter.in.web.dto.ResHealthProfileDto;
import com.workoutdone.rpgym.user.healthprofile.application.RegisterHealthProfileResult;
import com.workoutdone.rpgym.user.healthprofile.application.RegisterHealthProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/health-profiles")
@RequiredArgsConstructor
public class HealthProfileController {

    private final RegisterHealthProfileService registerHealthProfileService;

    // 본인 명의로만 등록 가능하므로 USER role만 허용 (ADMIN 제외)
    // X-User-Role 검증은 RoleAuthorizationInterceptor가 처리
    @RequireRole(UserRole.USER)
    @PostMapping("/me")
    public ResponseEntity<ResHealthProfileDto> registerHealthProfile(
            @RequestHeader(HeaderConstants.USER_ID) UUID userId,
            @Valid @RequestBody ReqRegisterHealthProfileDto request
    ) {
        RegisterHealthProfileResult result = registerHealthProfileService.registerHealthProfile(request.toCommand(userId));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResHealthProfileDto.from(result));
    }
}
