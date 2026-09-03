package com.workoutdone.rpgym.user.presentation.controller;

import com.workoutdone.rpgym.user.application.output.SignUpResult;
import com.workoutdone.rpgym.user.application.service.SignUpService;
import com.workoutdone.rpgym.user.presentation.dto.request.ReqSignUpDto;
import com.workoutdone.rpgym.user.presentation.dto.response.ResSignUpDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final SignUpService signUpService;

    @PostMapping("/signup")
    public ResponseEntity<ResSignUpDto> signUp(@Valid @RequestBody ReqSignUpDto request) {
        SignUpResult result = signUpService.signUp(request.toCommand());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResSignUpDto.from(result));
    }
}
