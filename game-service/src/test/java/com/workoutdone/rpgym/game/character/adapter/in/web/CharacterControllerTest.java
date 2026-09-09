package com.workoutdone.rpgym.game.character.adapter.in.web;

import com.workoutdone.rpgym.common.exception.GlobalExceptionHandler;
import com.workoutdone.rpgym.game.character.application.CharacterQueryUseCase;
import com.workoutdone.rpgym.game.character.application.CharacterView;
import com.workoutdone.rpgym.game.character.domain.CharacterTier;
import com.workoutdone.rpgym.game.exception.GameExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(CharacterController.class)
@Import({GameExceptionHandler.class, GlobalExceptionHandler.class})
class CharacterControllerTest {

    private static final String CHARACTERS_URL = "/api/v1/games/characters";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CharacterQueryUseCase characterQueryUseCase;

    @DisplayName("GET /api/v1/games/characters/me — 200")
    @Test
    void getMyCharacter() throws Exception {
        UUID userId = UUID.randomUUID();
        given(characterQueryUseCase.getCharacter(any()))
                .willReturn(new CharacterView(
                        userId, 7, CharacterTier.BRONZE, 820, 20, 100, 20.0, null, null));

        mockMvc.perform(get(CHARACTERS_URL + "/me")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value(7))
                .andExpect(jsonPath("$.tier").value("BRONZE"))
                .andExpect(jsonPath("$.totalXp").value(820))
                .andExpect(jsonPath("$.progressPercent").value(20.0));
    }

    @DisplayName("X-User-Id 헤더가 없으면 401 UNAUTHORIZED 를 반환한다")
    @Test
    void missingUserIdHeader() throws Exception {
        mockMvc.perform(get(CHARACTERS_URL + "/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @DisplayName("캐릭터 행이 없어도 404 가 아니라 200 으로 기본값을 반환한다")
    @Test
    void noCharacterRowStillReturns200() throws Exception {
        UUID userId = UUID.randomUUID();
        // 행은 없지만 XP 250 은 있는 상태. 0 으로 굳으면 안 된다 (CharacterQueryService 회귀 방지)
        given(characterQueryUseCase.getCharacter(any()))
                .willReturn(new CharacterView(
                        userId, 3, CharacterTier.BRONZE, 250, 50, 100, 50.0, null, null));

        mockMvc.perform(get(CHARACTERS_URL + "/me")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value(3))
                .andExpect(jsonPath("$.totalXp").value(250))
                .andExpect(jsonPath("$.createdAt").doesNotExist());
    }

    @DisplayName("PathVariable 이 UUID 가 아니면 400 INVALID_INPUT 을 반환한다")
    @Test
    void invalidPathVariable() throws Exception {
        mockMvc.perform(get(CHARACTERS_URL + "/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }
}
