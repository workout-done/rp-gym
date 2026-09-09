package com.workoutdone.rpgym.user.user.adapter.in.web;

import com.workoutdone.rpgym.common.constant.HeaderConstants;
import com.workoutdone.rpgym.common.exception.BaseException;
import com.workoutdone.rpgym.common.exception.CommonErrorCode;
import com.workoutdone.rpgym.user.security.RequireRole;
import com.workoutdone.rpgym.user.user.adapter.in.web.dto.ReqLoginDto;
import com.workoutdone.rpgym.user.user.adapter.in.web.dto.ReqSignUpDto;
import com.workoutdone.rpgym.user.user.adapter.in.web.dto.ResLoginDto;
import com.workoutdone.rpgym.user.user.adapter.in.web.dto.ResMyAccountDto;
import com.workoutdone.rpgym.user.user.adapter.in.web.dto.ResSignUpDto;
import com.workoutdone.rpgym.user.user.application.GetMyAccountResult;
import com.workoutdone.rpgym.user.user.application.GetMyAccountService;
import com.workoutdone.rpgym.user.user.application.LoginResult;
import com.workoutdone.rpgym.user.user.application.LoginService;
import com.workoutdone.rpgym.user.user.application.SignUpResult;
import com.workoutdone.rpgym.user.user.application.SignUpService;
import com.workoutdone.rpgym.user.user.domain.UserRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final SignUpService signUpService;
    private final LoginService loginService;
    private final GetMyAccountService getMyAccountService;

    @PostMapping("/signup")
    public ResponseEntity<ResSignUpDto> signUp(@Valid @RequestBody ReqSignUpDto request) {
        SignUpResult result = signUpService.signUp(request.toCommand());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResSignUpDto.from(result));
    }

    @PostMapping("/login")
    public ResponseEntity<ResLoginDto> login(@Valid @RequestBody ReqLoginDto request) {
        LoginResult result = loginService.login(request.toCommand());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResLoginDto.from(result));
    }

    // USER role만 본인 계정 조회 가능 (ADMIN 제외)
    // X-User-Role 검증은 RoleAuthorizationInterceptor가 처리
    @RequireRole(UserRole.USER)
    @GetMapping("/me")
    public ResponseEntity<ResMyAccountDto> getMyAccount(
            @RequestHeader(value = HeaderConstants.USER_ID, required = false) String userIdHeader
    ) {
        UUID userId = resolveUserId(userIdHeader);
        GetMyAccountResult result = getMyAccountService.getMyAccount(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResMyAccountDto.from(result));
    }

    //TO-DO: 게이트웨이에서 JWT 유효성 및 요청헤더 유효성 검증 로직 추가되면 해당 메서드는 삭제 예정
    // 게이트웨이를 거치지 않아 X-User-Id가 없거나 UUID 형식이 아니면 인증 안 된 요청으로 취급
    // 실제로 존재하는 사용자인지 여부는 GetMyAccountService.getMyAccount 에서 처리
    private UUID resolveUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new BaseException(CommonErrorCode.UNAUTHORIZED);
        }

        try {
            return UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
            throw new BaseException(CommonErrorCode.UNAUTHORIZED);
        }
    }
}
