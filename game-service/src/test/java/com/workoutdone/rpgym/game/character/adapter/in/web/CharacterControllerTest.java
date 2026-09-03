package com.workoutdone.rpgym.game.character.adapter.in.web;

import com.workoutdone.rpgym.game.character.application.CharacterQueryUseCase;
import com.workoutdone.rpgym.game.character.application.CharacterView;
import com.workoutdone.rpgym.game.character.domain.CharacterTier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(CharacterController.class)
class CharacterControllerTest {

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

        mockMvc.perform(get("/api/v1/games/characters/me")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value(7))
                .andExpect(jsonPath("$.tier").value("BRONZE"))
                .andExpect(jsonPath("$.totalXp").value(820))
                .andExpect(jsonPath("$.progressPercent").value(20.0));
    }

    @DisplayName("X-User-Id 헤더가 없으면 401 UNAUTHORIZED")
    @Test
    void missingHeader() throws Exception {
        mockMvc.perform(get("/api/v1/games/characters/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
}