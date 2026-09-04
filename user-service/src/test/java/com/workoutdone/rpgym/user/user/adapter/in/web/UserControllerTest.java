package com.workoutdone.rpgym.user.user.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workoutdone.rpgym.common.exception.BaseException;
import com.workoutdone.rpgym.user.user.adapter.in.web.dto.ReqSignUpDto;
import com.workoutdone.rpgym.user.user.application.LoginService;
import com.workoutdone.rpgym.user.user.application.SignUpCommand;
import com.workoutdone.rpgym.user.user.application.SignUpResult;
import com.workoutdone.rpgym.user.user.application.SignUpService;
import com.workoutdone.rpgym.user.user.domain.UserRole;
import com.workoutdone.rpgym.user.user.domain.UserStatus;
import com.workoutdone.rpgym.user.user.domain.UserErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    private static final String SIGNUP_URL = "/api/v1/users/signup";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SignUpService signUpService;

    // UserController가 LoginService에도 의존하므로, 컨텍스트 로딩을 위해 Mock으로 등록해야 한다.
    @MockitoBean
    private LoginService loginService;

    private ReqSignUpDto validRequest() {
        return ReqSignUpDto.builder()
                .email("healthuser@example.com")
                .password("myPassw0rd!")
                .nickname("헬스퀘스트유저")
                .slackId("U0123ABC456")
                .build();
    }

    @Test
    @DisplayName("정상 요청이면 201과 함께 회원 정보를 반환한다")
    void signUp_success() throws Exception {
        SignUpResult result = SignUpResult.builder()
                .id(UUID.randomUUID())
                .email("healthuser@example.com")
                .nickname("헬스퀘스트유저")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
        given(signUpService.signUp(any())).willReturn(result);

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("healthuser@example.com"))
                .andExpect(jsonPath("$.nickname").value("헬스퀘스트유저"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("이메일/닉네임/슬랙 아이디의 앞뒤 공백은 트리밍되고, 비밀번호는 트리밍되지 않은 채로 서비스에 전달된다")
    void signUp_trimsWhitespaceExceptPassword() throws Exception {
        SignUpResult result = SignUpResult.builder()
                .id(UUID.randomUUID())
                .email("healthuser@example.com")
                .nickname("헬스퀘스트유저")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
        given(signUpService.signUp(any())).willReturn(result);

        String rawJson = """
                {
                  "email": "  healthuser@example.com  ",
                  "password": "  myPassw0rd!  ",
                  "nickname": "  헬스퀘스트유저  ",
                  "slackId": "  U0123ABC456  "
                }
                """;

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawJson))
                .andExpect(status().isCreated());

        ArgumentCaptor<SignUpCommand> captor = ArgumentCaptor.forClass(SignUpCommand.class);
        verify(signUpService).signUp(captor.capture());
        SignUpCommand command = captor.getValue();

        assertThat(command.getEmail()).isEqualTo("healthuser@example.com");
        assertThat(command.getNickname()).isEqualTo("헬스퀘스트유저");
        assertThat(command.getSlackId()).isEqualTo("U0123ABC456");
        assertThat(command.getRawPassword()).isEqualTo("  myPassw0rd!  ");
    }

    @Test
    @DisplayName("이메일 형식이 아니면 400 INVALID_INPUT을 반환한다")
    void signUp_invalidEmail() throws Exception {
        ReqSignUpDto request = ReqSignUpDto.builder()
                .email("not-an-email")
                .password("myPassw0rd!")
                .nickname("닉네임")
                .slackId("U0123ABC456")
                .build();

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("비밀번호가 8자 미만이면 400 INVALID_INPUT을 반환한다")
    void signUp_passwordTooShort() throws Exception {
        ReqSignUpDto request = ReqSignUpDto.builder()
                .email("healthuser@example.com")
                .password("short")
                .nickname("닉네임")
                .slackId("U0123ABC456")
                .build();

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("닉네임이 비어 있으면 400 INVALID_INPUT을 반환한다")
    void signUp_blankNickname() throws Exception {
        ReqSignUpDto request = ReqSignUpDto.builder()
                .email("healthuser@example.com")
                .password("myPassw0rd!")
                .nickname("")
                .slackId("U0123ABC456")
                .build();

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("이메일이 이미 사용 중이면 409 EMAIL_DUPLICATED를 반환한다")
    void signUp_duplicateEmail() throws Exception {
        given(signUpService.signUp(any())).willThrow(new BaseException(UserErrorCode.EMAIL_DUPLICATED));

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_DUPLICATED"));
    }

    @Test
    @DisplayName("닉네임이 이미 사용 중이면 409 NICKNAME_DUPLICATED를 반환한다")
    void signUp_duplicateNickname() throws Exception {
        given(signUpService.signUp(any())).willThrow(new BaseException(UserErrorCode.NICKNAME_DUPLICATED));

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("NICKNAME_DUPLICATED"));
    }
}
